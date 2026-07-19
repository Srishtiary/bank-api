package com.bank.bank_api.service.impl;

import com.bank.bank_api.dto.DepositWithdrawRequest;
import com.bank.bank_api.dto.TransactionResponse;
import com.bank.bank_api.dto.TransferRequest;
import com.bank.bank_api.entity.Account;
import com.bank.bank_api.entity.AccountStatus;
import com.bank.bank_api.entity.Transaction;
import com.bank.bank_api.entity.TransactionStatus;
import com.bank.bank_api.entity.TransactionType;
import com.bank.bank_api.exception.AccountFrozenException;
import com.bank.bank_api.exception.AccountNotFoundException;
import com.bank.bank_api.exception.InsufficientBalanceException;
import com.bank.bank_api.repository.AccountRepository;
import com.bank.bank_api.repository.TransactionRepository;
import com.bank.bank_api.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(AccountRepository accountRepository,
                                  TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }


    @Override
    public TransactionResponse deposit(DepositWithdrawRequest request) {
        // 1. Find the account
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                        "No account found with account number: " + request.getAccountNumber()));

        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Account " + request.getAccountNumber() + " is frozen. Cannot deposit.");
        }

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(request.getAmount());
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setTimestamp(LocalDateTime.now());
        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapToTransactionResponse(savedTransaction, "Deposit successful.");
    }


    @Override
    public TransactionResponse withdraw(DepositWithdrawRequest request) {

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(
                        "No account found with account number: " + request.getAccountNumber()));


        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Account " + request.getAccountNumber() + " is frozen. Cannot withdraw.");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: " + account.getBalance() + ", Requested: " + request.getAmount());
        }


        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setAmount(request.getAmount());
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setTimestamp(LocalDateTime.now());
        Transaction savedTransaction = transactionRepository.save(transaction);


        return mapToTransactionResponse(savedTransaction, "Withdrawal successful.");
    }


    @Override
    @Transactional
    public TransactionResponse transferFunds(TransferRequest request) {
        String firstLock;
        String secondLock;

        if (request.getFromAccountNumber().compareTo(request.getToAccountNumber()) < 0) {
            firstLock = request.getFromAccountNumber();
            secondLock = request.getToAccountNumber();
        } else {
            firstLock = request.getToAccountNumber();
            secondLock = request.getFromAccountNumber();
        }


        Account firstAccount = accountRepository.findByAccountNumberForUpdate(firstLock)
                .orElseThrow(() -> new AccountNotFoundException("No account found with account number: " + firstLock));

        Account secondAccount = accountRepository.findByAccountNumberForUpdate(secondLock)
                .orElseThrow(() -> new AccountNotFoundException("No account found with account number: " + secondLock));


        Account senderAccount;
        Account receiverAccount;

        if (firstLock.equals(request.getFromAccountNumber())) {
            senderAccount = firstAccount;
            receiverAccount = secondAccount;
        } else {
            senderAccount = secondAccount;
            receiverAccount = firstAccount;
        }


        if (senderAccount.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Sender account " + request.getFromAccountNumber() + " is frozen.");
        }
        if (receiverAccount.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Receiver account " + request.getToAccountNumber() + " is frozen.");
        }


        if (senderAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: " + senderAccount.getBalance()
                    + ", Requested: " + request.getAmount());
        }


        BigDecimal senderNewBalance = senderAccount.getBalance().subtract(request.getAmount());
        BigDecimal receiverNewBalance = receiverAccount.getBalance().add(request.getAmount());

        senderAccount.setBalance(senderNewBalance);
        receiverAccount.setBalance(receiverNewBalance);

        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        Transaction senderTransaction = new Transaction();
        senderTransaction.setAccount(senderAccount);
        senderTransaction.setType(TransactionType.TRANSFER);
        senderTransaction.setAmount(request.getAmount());
        senderTransaction.setBalanceAfter(senderNewBalance);
        senderTransaction.setReferenceAccount(receiverAccount);
        senderTransaction.setStatus(TransactionStatus.SUCCESS);
        senderTransaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(senderTransaction);

        Transaction receiverTransaction = new Transaction();
        receiverTransaction.setAccount(receiverAccount);
        receiverTransaction.setType(TransactionType.TRANSFER);
        receiverTransaction.setAmount(request.getAmount());
        receiverTransaction.setBalanceAfter(receiverNewBalance);
        receiverTransaction.setReferenceAccount(senderAccount);
        receiverTransaction.setStatus(TransactionStatus.SUCCESS);
        receiverTransaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(receiverTransaction);


        return mapToTransactionResponse(senderTransaction,
                "Transfer of " + request.getAmount() + " to account " + request.getToAccountNumber() + " was successful.");
    }


    @Override
    public Page<TransactionResponse> getTransactionHistory(String accountNumber, Pageable pageable) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "No account found with account number: " + accountNumber));


        Page<Transaction> transactionPage =
                transactionRepository.findByAccountIdOrderByTimestampDesc(account.getId(), pageable);

        return transactionPage.map(transaction ->
                mapToTransactionResponse(transaction, "Transaction record."));
    }




    private TransactionResponse mapToTransactionResponse(Transaction transaction, String message) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setType(transaction.getType().name());
        response.setAmount(transaction.getAmount());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setTimestamp(transaction.getTimestamp());
        response.setStatus(transaction.getStatus().name());
        response.setMessage(message);
        return response;
    }
}
