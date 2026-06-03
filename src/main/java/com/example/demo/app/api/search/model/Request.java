package com.example.demo.app.api.search.model;

import lombok.Value;

/**
 * Запрос на внутренний поиск
 */
@Value
public class Request {
    /**
     * Идентификатор поискового запроса
     */
    String queryId;
}
