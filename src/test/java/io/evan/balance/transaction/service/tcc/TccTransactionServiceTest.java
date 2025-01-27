package io.evan.balance.transaction.service.tcc;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.evan.balance.account.AccountErrorCode;
import io.evan.balance.account.service.AccountInfo;
import io.evan.balance.account.service.AccountService;
import io.evan.balance.common.IdGenerator;
import io.evan.balance.common.Result;
import io.evan.balance.transaction.domain.Transaction;
import io.evan.balance.transaction.domain.TransactionRepository;
import io.evan.balance.transaction.error.TransactionErrorCode;
import io.evan.balance.transaction.service.TransferRequest;
import io.evan.balance.transaction.service.tcc.domain.TCCTransaction;
import io.evan.balance.transaction.service.tcc.domain.TCCTransactionRepository;
import io.evan.balance.transaction.service.tcc.domain.TCCTransactionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TccTransactionServiceTest {

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TCCTransactionRepository tccTransactionRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TccTransactionService tccTransactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCancelTransaction() {
        TransactionContext context = new TransactionContext("txn_123");
        Transaction transaction = Transaction.builder().sourceAccountNumber("acc_000001").build();
        TCCTransaction tccTransaction = mock(TCCTransaction.class);

        context.attachTransaction(transaction);
        context.attachTCCTransaction(tccTransaction);

        tccTransactionService.cancelTransaction(context);

        verify(accountService).unfreeze("txn_123", transaction.getSourceAccountNumber());
        verify(transactionRepository).save(transaction);
        verify(tccTransaction).setStatus(TCCTransactionStatus.CANCELLED);
        verify(tccTransactionRepository).save(tccTransaction);
    }

    @Test
    void testConfirmTransactionFailureOnAddBalance() {
        TransactionContext context = new TransactionContext("txn_123");
        Transaction transaction = Transaction.builder()
                .transactionId("txn_123")
                .sourceAccountNumber("acc_000001")
                .targetAccountNumber("acc_000002")
                .amount(new BigDecimal("100.00"))
                .status(Transaction.TransactionStatus.PENDING)
                .build();
        TCCTransaction tccTransaction = TCCTransaction.builder()
                .transactionId("txn_123")
                .status(TCCTransactionStatus.TRYING)
                .build();

        context.attachTransaction(transaction);
        context.attachTCCTransaction(tccTransaction);

        when(accountService.confirmFreeze(anyString(), anyString())).thenReturn(Result.success(new AccountInfo()));
        when(accountService.addBalance(anyString(), anyString(), any(BigDecimal.class), anyString())).thenReturn(Result.error(AccountErrorCode.ACCOUNT_NOT_FOUND));

        Result<Boolean, TransactionErrorCode> result = tccTransactionService.confirmTransaction(context);

        assertTrue(result.hasError());
        assertEquals(TransactionErrorCode.TARGET_ACCOUNT_NOT_FOUND, result.getError());
    }

    @Test
    void testConfirmTransactionFailureOnFreeze() {
        TransactionContext context = new TransactionContext("txn_123");
        Transaction transaction = Transaction.builder()
                .transactionId("txn_123")
                .sourceAccountNumber("acc_000001")
                .targetAccountNumber("acc_000002")
                .amount(new BigDecimal("100.00"))
                .status(Transaction.TransactionStatus.PENDING)
                .build();
        TCCTransaction tccTransaction = TCCTransaction.builder()
                .transactionId("txn_123")
                .status(TCCTransactionStatus.TRYING)
                .build();

        context.attachTransaction(transaction);
        context.attachTCCTransaction(tccTransaction);

        when(accountService.confirmFreeze(anyString(), anyString())).thenReturn(Result.error(AccountErrorCode.ACCOUNT_NOT_FOUND));

        Result<Boolean, TransactionErrorCode> result = tccTransactionService.confirmTransaction(context);

        assertTrue(result.hasError());
        assertEquals(TransactionErrorCode.SOURCE_ACCOUNT_NOT_FOUND, result.getError());
    }

    @Test
    void testConfirmTransactionSuccess() {
        TransactionContext context = new TransactionContext("txn_123");
        Transaction transaction = Transaction.builder()
                .transactionId("txn_123")
                .sourceAccountNumber("acc_000001")
                .targetAccountNumber("acc_000002")
                .amount(new BigDecimal("100.00"))
                .status(Transaction.TransactionStatus.PENDING)
                .build();
        TCCTransaction tccTransaction = TCCTransaction.builder()
                .transactionId("txn_123")
                .status(TCCTransactionStatus.TRYING)
                .build();

        context.attachTransaction(transaction);
        context.attachTCCTransaction(tccTransaction);

        when(accountService.confirmFreeze(anyString(), anyString())).thenReturn(Result.success(new AccountInfo()));
        when(accountService.addBalance(anyString(), anyString(), any(BigDecimal.class), anyString())).thenReturn(Result.success(new AccountInfo()));

        Result<Boolean, TransactionErrorCode> result = tccTransactionService.confirmTransaction(context);

        assertTrue(result.isSuccess());
        assertEquals(Transaction.TransactionStatus.COMPLETED, context.getTransaction().getStatus());
        assertEquals(TCCTransactionStatus.CONFIRMED, context.getTccTransaction().getStatus());
    }

    @Test
    void testCreateTransactionFailure() {
        when(tccTransactionRepository.save(any(TCCTransaction.class))).thenThrow(new RuntimeException("Transaction creation failed"));

        TransferRequest request = new TransferRequest("acc_000001", "acc_000002", new BigDecimal("100.00"));

        Result<TransactionContext, TransactionErrorCode> result = tccTransactionService.createTransaction(request);

        assertTrue(result.hasError());
        assertEquals(TransactionErrorCode.SYSTEM_ERROR, result.getError());
    }

    @Test
    void testCreateTransactionSuccess() {
        String transactionId = "txn_123";
        when(idGenerator.generate()).thenReturn(transactionId);

        TransferRequest request = new TransferRequest("acc_000001", "acc_000002", new BigDecimal("100.00"));

        TCCTransaction tccTransaction = TCCTransaction.builder()
                .transactionId(transactionId)
                .status(TCCTransactionStatus.TRYING)
                .build();
        when(tccTransactionRepository.save(any(TCCTransaction.class))).thenReturn(tccTransaction);

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .sourceAccountNumber(request.getSourceAccount())
                .targetAccountNumber(request.getTargetAccount())
                .amount(request.getAmount())
                .status(Transaction.TransactionStatus.PENDING)
                .build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Result<TransactionContext, TransactionErrorCode> result = tccTransactionService.createTransaction(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(transactionId, result.getData().getTransactionId());
        assertEquals(Transaction.TransactionStatus.PENDING, result.getData().getTransaction().getStatus());
        assertEquals(TCCTransactionStatus.TRYING, result.getData().getTccTransaction().getStatus());
    }

    @Test
    void testFinishTransactionFailureOnConfirm() {
        TransferRequest request = new TransferRequest("acc_000001", "acc_000002", new BigDecimal("100.00"));
        when(tccTransactionRepository.save(any(TCCTransaction.class))).thenReturn(new TCCTransaction());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
        when(accountService.freeze(anyString(), anyString(), any(BigDecimal.class), anyString())).thenReturn(Result.success(new AccountInfo()));
        when(accountService.confirmFreeze(anyString(), anyString())).thenReturn(Result.error(AccountErrorCode.ACCOUNT_NOT_FOUND));

        Result<Transaction, TransactionErrorCode> result = tccTransactionService.transfer(request);

        assertTrue(result.hasError());
        assertEquals(TransactionErrorCode.SYSTEM_ERROR, result.getError());
    }

    @Test
    void testFinishTransactionFailureOnTry() {
        TransferRequest request = new TransferRequest("acc_000001", "acc_000002", new BigDecimal("100.00"));
        when(tccTransactionRepository.save(any(TCCTransaction.class))).thenReturn(new TCCTransaction());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
        when(accountService.freeze(anyString(), anyString(), any(BigDecimal.class), anyString())).thenReturn(Result.error(AccountErrorCode.INSUFFICIENT_BALANCE));

        Result<Transaction, TransactionErrorCode> result = tccTransactionService.transfer(request);

        assertTrue(result.hasError());
        assertEquals(TransactionErrorCode.SYSTEM_ERROR, result.getError());
    }
}