package com.bank.bank_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    /**
     * Exposes BCryptPasswordEncoder as a Spring bean so it can be injected
     * into UserServiceImpl (and later into Spring Security's auth provider).
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
