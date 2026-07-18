package com.bank.bank_api.service.impl;

import com.bank.bank_api.dto.AccountResponse;
import com.bank.bank_api.dto.CreateAccountRequest;
import com.bank.bank_api.entity.Account;
import com.bank.bank_api.entity.AccountStatus;
import com.bank.bank_api.entity.AccountType;
import com.bank.bank_api.entity.User;
import com.bank.bank_api.exception.AccountNotFoundException;
import com.bank.bank_api.repository.AccountRepository;
import com.bank.bank_api.repository.UserRepository;
import com.bank.bank_api.service.AccountService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountServiceImpl(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AccountResponse createAccount(CreateAccountRequest request, String userEmail) {
        // 1. Find the user who owns this account
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AccountNotFoundException("No user found with email: " + userEmail));

        // 2. Generate a unique 12-digit account number
        String accountNumber = generateAccountNumber();

        // 3. Create the new Account entity using plain setters
        Account newAccount = new Account();
        newAccount.setAccountNumber(accountNumber);
        newAccount.setUser(user);
        newAccount.setBalance(BigDecimal.ZERO);
        newAccount.setAccountType(AccountType.valueOf(request.getAccountType().toUpperCase()));
        newAccount.setStatus(AccountStatus.ACTIVE);

        // 4. Save the account
        Account savedAccount = accountRepository.save(newAccount);

        // 5. Map the saved entity to a response DTO and return it
        return mapToAccountResponse(savedAccount);
    }

    @Override
    public AccountResponse getAccountDetails(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("No account found with account number: " + accountNumber));

        return mapToAccountResponse(account);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    /**
     * Generates a random 12-digit numeric string.
     * In production you would loop until a collision-free number is found;
     * for this project the randomness is sufficient.
     */
    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            accountNumber.append(random.nextInt(10)); // append a digit 0-9
        }
        return accountNumber.toString();
    }

    /**
     * Maps an Account entity to an AccountResponse DTO.
     * Converts enums to their String names so the caller gets plain text.
     */
    private AccountResponse mapToAccountResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setBalance(account.getBalance());
        response.setAccountType(account.getAccountType().name());
        response.setStatus(account.getStatus().name());
        return response;
    }
}
