package com.example.demo.exception;

import com.example.demo.common.BaseContext;

public class UsernameNotFoundException extends RuntimeException {
    public UsernameNotFoundException(String message) {
        super(message);
    }

}
