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
import io.evan.balance.transaction.error.TransactionException;
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
    public void cancelTransaction(final TransactionContext transactionContext) throws TransactionException {
        try {
            if (transactionContext == null || transactionContext.getTransaction() == null) {
                return;
            }

            accountService.unfreeze(
                    transactionContext.getTransactionId(),
                    transactionContext.getTransaction().getSourceAccountNumber()
            );

            // 更新事务状态
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
    public void confirmTransaction(final TransactionContext context) throws TransactionException {
        try {
            // 增加目标账户余额
            final Result<AccountInfo, AccountErrorCode> addBalanceResult =
                    this.accountService.addBalance(
                            context.getTransactionId(),
                            context.getTransaction().getTargetAccountNumber(),
                            context.getTransaction().getAmount(),
                            "Transfer In"
                    );

            if (addBalanceResult.hasError()) {
                switch (addBalanceResult.getError()) {
                    case ACCOUNT_NOT_FOUND -> throw new TransactionException(TransactionErrorCode.TARGET_ACCOUNT_NOT_FOUND);
                    default -> throw new TransactionException(TransactionErrorCode.TRANSACTION_STATUS_ERROR);
                }
            }

            // 执行实际转账
            // 解冻并扣减来源账户
            final Result<AccountInfo, AccountErrorCode> confirmFreezeResult =
                    this.accountService.confirmFreeze(
                            context.getTransactionId(),
                            context.getTransaction().getSourceAccountNumber()
                    );

            if (confirmFreezeResult.hasError()) {
                switch (confirmFreezeResult.getError()) {
                    case ACCOUNT_NOT_FOUND -> throw new TransactionException(TransactionErrorCode.SOURCE_ACCOUNT_NOT_FOUND);
                    case INSUFFICIENT_BALANCE -> throw new TransactionException(TransactionErrorCode.INSUFFICIENT_BALANCE);
                    default -> throw new TransactionException(TransactionErrorCode.TRANSACTION_STATUS_ERROR);
                }
            }

            // 更新事务状态
            context.getTransaction().setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(context.getTransaction());

            context.getTccTransaction().setStatus(TCCTransactionStatus.CONFIRMED);
            tccTransactionRepository.save(context.getTccTransaction());
        } catch (TransactionException e) {
            throw new TransactionException(e.getErrorCode());
        } catch (Exception e) {
            throw new TransactionException(TransactionErrorCode.TRANSACTION_STATUS_ERROR);
        }
    }

    @Transactional(rollbackFor = TransactionException.class)
    public TransactionContext createTransaction(final TransferRequest request) throws TransactionException {
        final String transactionId = this.idGenerator.generate();
        final TransactionContext transactionContext = new TransactionContext(transactionId);

        try {
            // 记录TCC的Transaction状态
            final TCCTransaction tccTransaction = tccTransactionRepository.save(
                    TCCTransaction
                            .builder()
                            .transactionId(transactionId)
                            .status(TCCTransactionStatus.TRYING)
                            .build()
            );

            transactionContext.attachTCCTransaction(tccTransaction);

            // 记录交易
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

            return transactionContext;
        } catch (Exception e) {
            throw new TransactionException(TransactionErrorCode.SYSTEM_ERROR);
        }
    }

    public Transaction finishTransaction(final String transactionId) throws TransactionException {
        final TransactionContext transactionContext = new TransactionContext(transactionId);
        try {
            this.transactionRepository.findByTransactionId(transactionId)
                    .ifPresent(transactionContext::attachTransaction);

            this.tccTransactionRepository.findByTransactionId(transactionId)
                    .ifPresent(transactionContext::attachTCCTransaction);

            tryTransaction(transactionContext);
            confirmTransaction(transactionContext);

            return transactionContext.getTransaction();
        } catch (TransactionException e) {
            cancelTransaction(transactionContext);
            throw e;
        }
    }

    @Override
    public Transaction transfer(final TransferRequest request) throws TransactionException {
        TransactionContext transactionContext = null;
        try {
            transactionContext = createTransaction(request);
            tryTransaction(transactionContext);
            confirmTransaction(transactionContext);

            return transactionContext.getTransaction();
        } catch (TransactionException e) {
            cancelTransaction(transactionContext);
            throw new TransactionException(e.getErrorCode());
        }
    }

    public void tryTransaction(TransactionContext transactionContext) throws TransactionException {
        try {
            // 冻结转出方资金
            Result<AccountInfo, AccountErrorCode> frozen = accountService.freeze(
                    transactionContext.getTransactionId(),
                    transactionContext.getTransaction().getSourceAccountNumber(),
                    transactionContext.getTransaction().getAmount(),
                    "Transfer Out"
            );

            var checkSourceAccountFrozenResult = checkSourceAccountFrozenResult(frozen);
            if (checkSourceAccountFrozenResult.hasError()) {
                throw new TransactionException(checkSourceAccountFrozenResult.getError());
            }
        } catch (Exception e) {
            throw new TransactionException(TransactionErrorCode.SYSTEM_ERROR);
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
