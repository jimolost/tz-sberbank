package com.example.demo.app.impl.search;

import java.util.List;

/**
 * Результат вызова
 */
record SearchCallResult(List<String> values, String errorCode) {

    static SearchCallResult success(List<String> values) {
        return new SearchCallResult(values, null);
    }

    static SearchCallResult error(String errorCode) {
        return new SearchCallResult(null, errorCode);
    }

    boolean isError() {
        return errorCode != null;
    }
}
