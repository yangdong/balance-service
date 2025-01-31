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
import static org.mockito.Mockito.times;
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
    void testCreateTransaction() throws Exception {
        // Arrange
        String transactionId = "txn_123";
        when(idGenerator.generate()).thenReturn(transactionId);
        when(tccTransactionRepository.save(any(TCCTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransferRequest request = new TransferRequest("acc_000001", "acc_000002", new BigDecimal("100"));

        // Act
        TransactionContext transactionContext = tccTransactionService.createTransaction(request);

        // Assert
        assertNotNull(transactionContext);
        assertEquals(transactionId, transactionContext.getTransactionId());
        assertNotNull(transactionContext.getTransaction());
        assertNotNull(transactionContext.getTccTransaction());
        assertEquals(TCCTransactionStatus.TRYING, transactionContext.getTccTransaction().getStatus());
    }

    @Test
    void testConfirmTransaction() throws Exception {
        // Arrange
        String transactionId = "txn_123";
        Transaction transaction = Transaction.builder()
                .sourceAccountNumber("acc_001")
                .targetAccountNumber("acc_001")
                .amount(new BigDecimal("1.0"))
                .build();
        TCCTransaction tccTransaction = mock(TCCTransaction.class);
        TransactionContext context = new TransactionContext(transactionId);
        context.attachTransaction(transaction);
        context.attachTCCTransaction(tccTransaction);

        when(accountService.addBalance(anyString(), anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(Result.success(mock(AccountInfo.class)));
        when(accountService.confirmFreeze(anyString(), anyString()))
                .thenReturn(Result.success(mock(AccountInfo.class)));

        // Act
        tccTransactionService.confirmTransaction(context);

        // Assert
        assertEquals(Transaction.TransactionStatus.COMPLETED, transaction.getStatus());
        verify(transactionRepository).save(transaction);
        verify(tccTransactionRepository).save(tccTransaction);
    }

    @Test
    void testCancelTransaction() throws Exception {
        // Arrange
        String transactionId = "txn_123";
        Transaction transaction = mock(Transaction.class);
        TCCTransaction tccTransaction = mock(TCCTransaction.class);
        TransactionContext context = new TransactionContext(transactionId);
        context.attachTransaction(transaction);
        context.attachTCCTransaction(tccTransaction);

        // Act
        tccTransactionService.cancelTransaction(context);

        // Assert
        verify(accountService).unfreeze(transactionId, transaction.getSourceAccountNumber());
        verify(transaction).setStatus(Transaction.TransactionStatus.FAILED);
        verify(tccTransaction).setStatus(TCCTransactionStatus.CANCELLED);
        verify(transactionRepository).save(transaction);
        verify(tccTransactionRepository).save(tccTransaction);
    }

    @Test
    void testFinishTransaction() throws Exception {
        // Arrange
        String transactionId = "txn_123";
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .sourceAccountNumber("acc_001")
                .targetAccountNumber("acc_002")
                .amount(new BigDecimal("100"))
                .status(Transaction.TransactionStatus.PENDING)
                .build();
        TCCTransaction tccTransaction = TCCTransaction.builder()
                .transactionId(transactionId)
                .status(TCCTransactionStatus.TRYING)
                .build();

        when(transactionRepository.findByTransactionId(transactionId))
                .thenReturn(java.util.Optional.of(transaction));
        when(tccTransactionRepository.findByTransactionId(transactionId))
                .thenReturn(java.util.Optional.of(tccTransaction));
        when(accountService.freeze(anyString(), anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(Result.success(mock(AccountInfo.class)));
        when(accountService.addBalance(anyString(), anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(Result.success(mock(AccountInfo.class)));
        when(accountService.confirmFreeze(anyString(), anyString()))
                .thenReturn(Result.success(mock(AccountInfo.class)));

        // Act
        Transaction result = tccTransactionService.finishTransaction(transactionId);

        // Assert
        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
        verify(transactionRepository).save(transaction);
        verify(tccTransactionRepository).save(tccTransaction);
    }

    @Test
    void testTransfer() throws Exception {
        // Arrange
        String transactionId = "txn_123";
        when(idGenerator.generate()).thenReturn(transactionId);
        when(tccTransactionRepository.save(any(TCCTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(accountService.freeze(anyString(), anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(Result.success(mock(AccountInfo.class)));
        when(accountService.addBalance(anyString(), anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(Result.success(mock(AccountInfo.class)));
        when(accountService.confirmFreeze(anyString(), anyString()))
                .thenReturn(Result.success(mock(AccountInfo.class)));

        TransferRequest request = new TransferRequest("acc_000001", "acc_000002", new BigDecimal("100"));

        // Act
        Transaction result = tccTransactionService.transfer(request);

        // Assert
        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(tccTransactionRepository, times(2)).save(any(TCCTransaction.class));
    }
}