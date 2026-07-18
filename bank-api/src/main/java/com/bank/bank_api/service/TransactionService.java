package com.bank.bank_api.service;

import com.bank.bank_api.dto.DepositWithdrawRequest;
import com.bank.bank_api.dto.TransactionResponse;
import com.bank.bank_api.dto.TransferRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {

    TransactionResponse deposit(DepositWithdrawRequest request);

    TransactionResponse withdraw(DepositWithdrawRequest request);

    TransactionResponse transferFunds(TransferRequest request);

    Page<TransactionResponse> getTransactionHistory(String accountNumber, Pageable pageable);
}
