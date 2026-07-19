package com.bank.bank_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotBlank(message = "Source account number must not be blank")
    private String fromAccountNumber;

    @NotBlank(message = "Destination account number must not be blank")
    private String toAccountNumber;

    @NotNull(message = "Amount must not be null")
    @Positive(message = "Amount must be a positive number")
    private BigDecimal amount;
}
