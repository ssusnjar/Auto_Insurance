package com.truenorth.backend.exception;

import lombok.Getter;

@Getter
public class SqlQueryFailedException extends RuntimeException {
    private final String failedQuery;

    public SqlQueryFailedException(String message, Throwable cause, String failedQuery) {
        super(message, cause);
        this.failedQuery = failedQuery;
    }
}