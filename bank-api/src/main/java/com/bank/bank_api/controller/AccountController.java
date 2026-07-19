package com.bank.bank_api.controller;

import com.bank.bank_api.dto.AccountResponse;
import com.bank.bank_api.dto.CreateAccountRequest;
import com.bank.bank_api.security.SecurityUtils;
import com.bank.bank_api.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    // Constructor injection — no @Autowired needed with a single constructor
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        AccountResponse response = accountService.createAccount(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountDetails(@PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccountDetails(accountNumber);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
