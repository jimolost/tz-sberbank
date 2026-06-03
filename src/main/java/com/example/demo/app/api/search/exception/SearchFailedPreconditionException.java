package com.example.demo.app.api.search.exception;

import com.example.demo.app.api.search.SearchErrorCodes;
import lombok.Getter;

/**
 * Нет данных, есть ошибка, отличная от NOT_FOUND
 */
@Getter
public class SearchFailedPreconditionException extends RuntimeException {
    private final String errorCode;

    public SearchFailedPreconditionException() {
        super("Search failed: external service returned non-recoverable errors");
        this.errorCode = SearchErrorCodes.FAILED_PRECONDITION;
    }
}
