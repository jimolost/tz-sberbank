package com.example.demo.app.impl.search;

import com.example.demo.app.api.search.ExternalSearchOutbound;
import com.example.demo.app.api.search.SearchBdIndexes;
import com.example.demo.app.api.search.SearchErrorCodes;
import com.example.demo.app.api.search.exception.ExternalSearchException;
import com.example.demo.app.api.search.exception.SearchFailedPreconditionException;
import com.example.demo.app.api.search.exception.SearchNotFoundException;
import com.example.demo.app.api.search.model.Request;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Внутренний поиск
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InternalSearchService {
    private final ExternalSearchOutbound externalSearchOutbound;
    private final Executor searchExecutor;

    /**
     * Поиск по всем индексам параллельно и возвращает консолидированный результат
     */
    public String internalSearch(Request request) {
        log.info("InternalSearchService#internalSearch started for queryId={}", request.getQueryId());
        List<SearchCallResult> callResults = searchInAllIndexes(request);
        return consolidate(callResults);
    }

    // ===================================================================================================================
    // = Implementation
    // ===================================================================================================================

    private List<SearchCallResult> searchInAllIndexes(Request request) {
        List<CompletableFuture<SearchCallResult>> futures = createSearchFutures(request);
        return collectSearchResults(futures);
    }

    private List<CompletableFuture<SearchCallResult>> createSearchFutures(Request request) {
        return SearchBdIndexes.ALL.stream()
                .map(bdIndex -> createSearchFuture(bdIndex, request))
                .toList();
    }

    private CompletableFuture<SearchCallResult> createSearchFuture(String bdIndex, Request request) {
        return CompletableFuture.supplyAsync(() -> searchInIndex(bdIndex, request), searchExecutor);
    }

    private List<SearchCallResult> collectSearchResults(List<CompletableFuture<SearchCallResult>> futures) {
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private SearchCallResult searchInIndex(String bdIndex, Request request) {
        try {
            List<String> values = externalSearchOutbound.externalServiceSearch(bdIndex, request);
            return SearchCallResult.success(values);
        } catch (ExternalSearchException e) {
            log.warn("External search failed for bdIndex={}, errorCode={}", bdIndex, e.getErrorCode());
            return SearchCallResult.error(e.getErrorCode());
        }
    }

    private String consolidate(List<SearchCallResult> callResults) {
        Optional<String> foundValue = findFirstFoundValue(callResults);
        if (foundValue.isPresent()) {
            return foundValue.get();
        }
        if (hasNonNotFoundError(callResults)) {
            throw new SearchFailedPreconditionException();
        }
        throw new SearchNotFoundException();
    }

    private Optional<String> findFirstFoundValue(List<SearchCallResult> callResults) {
        return callResults.stream()
                .map(SearchCallResult::values)
                .filter(values -> values != null && !values.isEmpty())
                .map(List::getFirst)
                .findFirst();
    }

    private boolean hasNonNotFoundError(List<SearchCallResult> callResults) {
        return callResults.stream()
                .filter(SearchCallResult::isError)
                .map(SearchCallResult::errorCode)
                .anyMatch(code -> !SearchErrorCodes.NOT_FOUND.equals(code));
    }
}