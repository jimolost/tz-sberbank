package com.example.demo.app.api.search.exception;

import com.example.demo.app.api.search.SearchErrorCodes;
import lombok.Getter;

/**
 * Все ответы пустые или только NOT_FOUND
 */
@Getter
public class SearchNotFoundException extends RuntimeException {
    private final String errorCode;

    public SearchNotFoundException() {
        super("Search result not found in any bd index");
        this.errorCode = SearchErrorCodes.NOT_FOUND;
    }
}
