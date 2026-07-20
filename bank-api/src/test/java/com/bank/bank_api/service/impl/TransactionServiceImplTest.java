package com.bank.bank_api.service.impl;

import com.bank.bank_api.dto.TransferRequest;
import com.bank.bank_api.dto.TransactionResponse;
import com.bank.bank_api.entity.Account;
import com.bank.bank_api.entity.AccountStatus;
import com.bank.bank_api.exception.AccountFrozenException;
import com.bank.bank_api.exception.InsufficientBalanceException;
import com.bank.bank_api.repository.AccountRepository;
import com.bank.bank_api.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void transferFunds_Success() {
        // Given: Two accounts with sufficient balance
        Account sender = new Account();
        sender.setId(1L);
        sender.setAccountNumber("ACC1");
        sender.setBalance(new BigDecimal("1000.00"));
        sender.setStatus(AccountStatus.ACTIVE);

        Account receiver = new Account();
        receiver.setId(2L);
        receiver.setAccountNumber("ACC2");
        receiver.setBalance(new BigDecimal("500.00"));
        receiver.setStatus(AccountStatus.ACTIVE);

        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("ACC1");
        request.setToAccountNumber("ACC2");
        request.setAmount(new BigDecimal("200.00"));

        when(accountRepository.findByAccountNumberForUpdate("ACC1")).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumberForUpdate("ACC2")).thenReturn(Optional.of(receiver));


        TransactionResponse response = transactionService.transferFunds(request);


        assertEquals(new BigDecimal("800.00"), sender.getBalance());
        assertEquals(new BigDecimal("700.00"), receiver.getBalance());
        assertNotNull(response);
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(2)).save(any());
    }

    @Test
    void transferFunds_InsufficientBalance_ThrowsException() {
        // Given: Sender account has insufficient balance
        Account sender = new Account();
        sender.setId(1L);
        sender.setAccountNumber("ACC1");
        sender.setBalance(new BigDecimal("100.00"));
        sender.setStatus(AccountStatus.ACTIVE);

        Account receiver = new Account();
        receiver.setId(2L);
        receiver.setAccountNumber("ACC2");
        receiver.setBalance(new BigDecimal("500.00"));
        receiver.setStatus(AccountStatus.ACTIVE);

        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("ACC1");
        request.setToAccountNumber("ACC2");
        request.setAmount(new BigDecimal("200.00"));

        when(accountRepository.findByAccountNumberForUpdate("ACC1")).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumberForUpdate("ACC2")).thenReturn(Optional.of(receiver));

        InsufficientBalanceException exception = assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.transferFunds(request)
        );

        assertNotNull(exception);
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transferFunds_AccountFrozen_ThrowsException() {
        // Given: The sender account is frozen
        Account sender = new Account();
        sender.setId(1L);
        sender.setAccountNumber("ACC1");
        sender.setBalance(new BigDecimal("1000.00"));
        sender.setStatus(AccountStatus.FROZEN);

        Account receiver = new Account();
        receiver.setId(2L);
        receiver.setAccountNumber("ACC2");
        receiver.setBalance(new BigDecimal("500.00"));
        receiver.setStatus(AccountStatus.ACTIVE);

        TransferRequest request = new TransferRequest();
        request.setFromAccountNumber("ACC1");
        request.setToAccountNumber("ACC2");
        request.setAmount(new BigDecimal("200.00"));

        when(accountRepository.findByAccountNumberForUpdate("ACC1")).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumberForUpdate("ACC2")).thenReturn(Optional.of(receiver));


        AccountFrozenException exception = assertThrows(
                AccountFrozenException.class,
                () -> transactionService.transferFunds(request)
        );


        assertNotNull(exception);
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any());
    }
}
