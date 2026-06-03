package com.example.demo.app.api.search.exception;

import lombok.Getter;

/**
 * Ошибка ответа внешнего сервиса поиска
 */
@Getter
public class ExternalSearchException extends RuntimeException {
    private final String errorCode;

    public ExternalSearchException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
