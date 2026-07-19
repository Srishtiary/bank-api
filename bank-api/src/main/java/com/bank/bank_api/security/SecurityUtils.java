package com.bank.bank_api.security;

import org.springframework.security.core.context.SecurityContextHolder;


public class SecurityUtils {


    private SecurityUtils() {}

    public static String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
