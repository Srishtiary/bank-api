package com.bank.bank_api.controller;

import com.bank.bank_api.dto.AccountResponse;
import com.bank.bank_api.dto.UserSummaryResponse;
import com.bank.bank_api.entity.Account;
import com.bank.bank_api.entity.AccountStatus;
import com.bank.bank_api.entity.User;
import com.bank.bank_api.exception.AccountNotFoundException;
import com.bank.bank_api.repository.AccountRepository;
import com.bank.bank_api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AdminController(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<UserSummaryResponse> response = users.stream()
                .map(user -> new UserSummaryResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole().name()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();

        List<AccountResponse> response = accounts.stream()
                .map(account -> new AccountResponse(
                        account.getId(),
                        account.getAccountNumber(),
                        account.getBalance(),
                        account.getAccountType().name(),
                        account.getStatus().name()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PutMapping("/accounts/{accountNumber}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> freezeAccount(@PathVariable String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);

        return ResponseEntity.status(HttpStatus.OK).body("Account " + accountNumber + " has been frozen.");
    }


    @PutMapping("/accounts/{accountNumber}/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> unfreezeAccount(@PathVariable String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        return ResponseEntity.status(HttpStatus.OK).body("Account " + accountNumber + " has been activated.");
    }
}
