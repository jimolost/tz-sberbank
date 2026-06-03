package com.example.demo.app.api.search;

import com.example.demo.app.api.search.model.Request;

import java.util.List;

public interface ExternalSearchOutbound {
    /**
     * Поиск во внешнем сервисе по индексу БД
     */
    List<String> externalServiceSearch(String bdIndex, Request request);
}
