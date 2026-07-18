package com.bank.bank_api.service;

import com.bank.bank_api.dto.AccountResponse;
import com.bank.bank_api.dto.CreateAccountRequest;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest request, String userEmail);

    AccountResponse getAccountDetails(String accountNumber);
}
