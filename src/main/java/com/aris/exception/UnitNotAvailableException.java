package com.aris.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UnitNotAvailableException extends RuntimeException {
    public UnitNotAvailableException(String message) {
        super(message);
    }
}
