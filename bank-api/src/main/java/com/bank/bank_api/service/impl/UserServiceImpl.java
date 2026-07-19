package com.bank.bank_api.service.impl;

import com.bank.bank_api.dto.RegisterRequest;
import com.bank.bank_api.entity.Role;
import com.bank.bank_api.entity.User;
import com.bank.bank_api.exception.AccountNotFoundException;
import com.bank.bank_api.exception.UserAlreadyExistsException;
import com.bank.bank_api.repository.UserRepository;
import com.bank.bank_api.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(RegisterRequest request) {

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("A user with email " + request.getEmail() + " already exists.");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());


        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(hashedPassword);
        newUser.setRole(Role.USER);


        return userRepository.save(newUser);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("No user found with email: " + email));
    }
}
