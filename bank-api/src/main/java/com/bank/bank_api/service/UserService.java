package com.bank.bank_api.service;

import com.bank.bank_api.dto.RegisterRequest;
import com.bank.bank_api.entity.User;

public interface UserService {

    User register(RegisterRequest request);

    User findByEmail(String email);
}
