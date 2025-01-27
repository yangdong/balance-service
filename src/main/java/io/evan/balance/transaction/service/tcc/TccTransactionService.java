package io.evan.balance.transaction.service.tcc;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.evan.balance.account.AccountErrorCode;
import io.evan.balance.account.service.AccountInfo;
import io.evan.balance.account.service.AccountService;
import io.evan.balance.common.IdGenerator;
import io.evan.balance.common.Result;
import io.evan.balance.transaction.domain.Transaction;
import io.evan.balance.transaction.domain.TransactionRepository;
import io.evan.balance.transaction.error.TransactionErrorCode;
import io.evan.balance.transaction.service.TransactionService;
import io.evan.balance.transaction.service.TransferRequest;
import io.evan.balance.transaction.service.tcc.domain.TCCTransaction;
import io.evan.balance.transaction.service.tcc.domain.TCCTransactionRepository;
import io.evan.balance.transaction.service.tcc.domain.TCCTransactionStatus;

@Component
public class TccTransactionService implements TransactionService {
    private final IdGenerator idGenerator;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    //Only for TCC implementation
    private final TCCTransactionRepository tccTransactionRepository;

    public TccTransactionService(final IdGenerator idGenerator,
                                 final TransactionRepository transactionRepository,
                                 final TCCTransactionRepository tccTransactionRepository,
                                 final AccountService accountService) {
        this.idGenerator = idGenerator;
        this.transactionRepository = transactionRepository;
        this.tccTransactionRepository = tccTransactionRepository;
        this.accountService = accountService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelTransaction(final TransactionContext transactionContext) {
        try {
            if (transactionContext.getTransaction() == null) {
                return;
            }

            accountService.unfreeze(
                    transactionContext.getTransactionId(),
                    transactionContext.getTransaction().getSourceAccountNumber()
            );

            // 5. 更新事务状态
            transactionContext.getTransaction().setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transactionContext.getTransaction());

            transactionContext.getTccTransaction().setStatus(TCCTransactionStatus.CANCELLED);
            tccTransactionRepository.save(transactionContext.getTccTransaction());

            return;
        } catch (Exception e) {
            return;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean, TransactionErrorCode> confirmTransaction(final TransactionContext context) {
        try {
            // 1. 执行实际转账
            // 2. 解冻并扣减来源账户
            final Result<AccountInfo, AccountErrorCode> confirmFreezeResult =
                    this.accountService.confirmFreeze(
                            context.getTransactionId(),
                            context.getTransaction().getSourceAccountNumber()
                    );

            if (confirmFreezeResult.hasError()) {
                return switch (confirmFreezeResult.getError()) {
                    case ACCOUNT_NOT_FOUND -> Result.error(TransactionErrorCode.SOURCE_ACCOUNT_NOT_FOUND);
                    case INSUFFICIENT_BALANCE -> Result.error(TransactionErrorCode.INSUFFICIENT_BALANCE);
                    default -> Result.error(TransactionErrorCode.TRANSACTION_STATUS_ERROR);
                };
            }

            // 3. 增加目标账户余额
            final Result<AccountInfo, AccountErrorCode> addBalanceResult =
                    this.accountService.addBalance(
                            context.getTransactionId(),
                            context.getTransaction().getTargetAccountNumber(),
                            context.getTransaction().getAmount(),
                            "Transfer In"
                    );

            if (addBalanceResult.hasError()) {
                return switch (addBalanceResult.getError()) {
                    case ACCOUNT_NOT_FOUND -> Result.error(TransactionErrorCode.TARGET_ACCOUNT_NOT_FOUND);
                    default -> Result.error(TransactionErrorCode.TRANSACTION_STATUS_ERROR);
                };
            }

            // 4. 更新事务状态
            context.getTransaction().setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(context.getTransaction());

            context.getTccTransaction().setStatus(TCCTransactionStatus.CONFIRMED);
            tccTransactionRepository.save(context.getTccTransaction());

            return Result.success(true);
        } catch (Exception e) {
            return Result.error(TransactionErrorCode.TRANSACTION_STATUS_ERROR);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<TransactionContext, TransactionErrorCode> createTransaction(final TransferRequest request) {
        final String transactionId = this.idGenerator.generate();
        final TransactionContext transactionContext = new TransactionContext(transactionId);

        try {
            // 1. 记录TCC的Transaction状态
            final TCCTransaction tccTransaction = tccTransactionRepository.save(
                    TCCTransaction
                            .builder()
                            .transactionId(transactionId)
                            .status(TCCTransactionStatus.TRYING)
                            .build()
            );

            transactionContext.attachTCCTransaction(tccTransaction);

            // 2. 记录交易
            final Transaction transaction = transactionRepository.save(
                    Transaction.builder()
                            .sourceAccountNumber(request.getSourceAccount())
                            .targetAccountNumber(request.getTargetAccount())
                            .amount(request.getAmount())
                            .timestamp(LocalDateTime.now())
                            .transactionId(transactionId)
                            .status(Transaction.TransactionStatus.PENDING)
                            .build()
            );

            transactionContext.attachTransaction(transaction);

            return Result.success(transactionContext);
        } catch (Exception e) {
            return Result.error(TransactionErrorCode.SYSTEM_ERROR);
        }
    }

    public Result<Transaction, TransactionErrorCode> finishTransaction(final String transactionId) {
        final TransactionContext transactionContext = new TransactionContext(transactionId);

        this.transactionRepository.findByTransactionId(transactionId)
                .ifPresent(transactionContext::attachTransaction);

        this.tccTransactionRepository.findByTransactionId(transactionId)
                .ifPresent(transactionContext::attachTCCTransaction);

        return finishTransaction(transactionContext);
    }

    public Result<Transaction, TransactionErrorCode> finishTransaction(final TransactionContext transactionContext) {
        final Result<TransactionContext, TransactionErrorCode> tryResult = tryTransaction(transactionContext);
        if (tryResult.hasError()) {
            cancelTransaction(tryResult.getData());
            return Result.error(tryResult.getError());
        }

        final Result<Boolean, TransactionErrorCode> confirmResult = confirmTransaction(transactionContext);

        if (confirmResult.hasError()) {
            cancelTransaction(transactionContext);
            return Result.error(confirmResult.getError());
        }

        return Result.success(transactionContext.getTransaction());
    }

    @Override
    public Result<Transaction, TransactionErrorCode> transfer(final TransferRequest request) {
        final Result<TransactionContext, TransactionErrorCode> transactionResult = createTransaction(request);

        if (transactionResult.hasError()) {
            return Result.error(transactionResult.getError());
        }

        final TransactionContext transactionContext = transactionResult.getData();

        return finishTransaction(transactionContext);
    }

    public Result<TransactionContext, TransactionErrorCode> tryTransaction(TransactionContext transactionContext) {
        try {
            // 1. 冻结转出方资金
            Result<AccountInfo, AccountErrorCode> frozen = accountService.freeze(
                    transactionContext.getTransactionId(),
                    transactionContext.getTransaction().getSourceAccountNumber(),
                    transactionContext.getTransaction().getAmount(),
                    "Transfer Out"
            );

            var checkSourceAccountFrozenResult = checkSourceAccountFrozenResult(frozen);
            if (checkSourceAccountFrozenResult.hasError()) {
                return new Result<>(transactionContext, checkSourceAccountFrozenResult.getError());
            }

            return Result.success(transactionContext);
        } catch (Exception e) {
            cancelTransaction(transactionContext);
            return new Result<>(transactionContext, TransactionErrorCode.SYSTEM_ERROR);
        }
    }

    private Result<Boolean, TransactionErrorCode> checkSourceAccountFrozenResult(final Result<AccountInfo, AccountErrorCode> frozen) {
        if (frozen.isSuccess()) {
            return Result.success(true);
        }

        return switch (frozen.getError()) {
            case INSUFFICIENT_BALANCE:
                yield Result.error(TransactionErrorCode.INSUFFICIENT_BALANCE);
            case ACCOUNT_NOT_FOUND:
                yield Result.error(TransactionErrorCode.SOURCE_ACCOUNT_NOT_FOUND);
            default:
                yield Result.error(TransactionErrorCode.SYSTEM_ERROR);
        };
    }
}
