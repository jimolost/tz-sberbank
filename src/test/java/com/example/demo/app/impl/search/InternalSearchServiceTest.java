package com.example.demo.app.impl.search;

import com.example.demo.app.api.search.ExternalSearchOutbound;
import com.example.demo.app.api.search.exception.ExternalSearchException;
import com.example.demo.app.api.search.exception.SearchFailedPreconditionException;
import com.example.demo.app.api.search.exception.SearchNotFoundException;
import com.example.demo.app.api.search.model.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.demo.app.api.search.SearchBdIndexes.*;
import static com.example.demo.app.api.search.SearchErrorCodes.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalSearchServiceTest {
    private static final String QUERY_ID = "2026-0603-0042817";
    private static final String FOUND_CLIENT_ID = "90817263";
    private static final List<String> EMPTY_SEARCH_RESULT = List.of();
    private static final List<String> NON_EMPTY_SEARCH_RESULT = List.of(FOUND_CLIENT_ID);
    private static final Request SEARCH_REQUEST = new Request(QUERY_ID);
    private static final ExternalSearchException NOT_FOUND_EXCEPTION = new ExternalSearchException(NOT_FOUND, "Client not found in index");
    private static final ExternalSearchException SERVICE_UNAVAILABLE_EXCEPTION = new ExternalSearchException("UNAVAILABLE", "External search service unavailable");

    @Mock
    private ExternalSearchOutbound externalSearchOutbound;

    private ExecutorService searchExecutor;
    private InternalSearchService internalSearchService;

    @BeforeEach
    void setUp() {
        searchExecutor = Executors.newFixedThreadPool(ALL.size());
        internalSearchService = new InternalSearchService(externalSearchOutbound, searchExecutor);
    }

    @AfterEach
    void tearDown() {
        searchExecutor.shutdown();
    }

    @Test
    void internalSearch_allEmpty_returnsNotFound() {
        when(externalSearchOutbound.externalServiceSearch(any(), eq(SEARCH_REQUEST))).thenReturn(EMPTY_SEARCH_RESULT);

        assertThrows(SearchNotFoundException.class, () -> internalSearchService.internalSearch(SEARCH_REQUEST));
    }

    @Test
    void internalSearch_allNotFound_returnsNotFound() {
        when(externalSearchOutbound.externalServiceSearch(any(), eq(SEARCH_REQUEST))).thenThrow(NOT_FOUND_EXCEPTION);

        assertThrows(SearchNotFoundException.class, () -> internalSearchService.internalSearch(SEARCH_REQUEST));
    }

    @Test
    void internalSearch_emptyAndNotFound_returnsNotFound() {
        when(externalSearchOutbound.externalServiceSearch(eq(CLIENT_MDM), eq(SEARCH_REQUEST))).thenReturn(EMPTY_SEARCH_RESULT);
        when(externalSearchOutbound.externalServiceSearch(eq(CLIENT_PKB), eq(SEARCH_REQUEST))).thenThrow(NOT_FOUND_EXCEPTION);
        when(externalSearchOutbound.externalServiceSearch(eq(CLIENT_CFT), eq(SEARCH_REQUEST))).thenReturn(EMPTY_SEARCH_RESULT);

        assertThrows(SearchNotFoundException.class, () -> internalSearchService.internalSearch(SEARCH_REQUEST));
    }

    @Test
    void internalSearch_oneNonEmpty_returnsFirstElement() {
        when(externalSearchOutbound.externalServiceSearch(eq(CLIENT_MDM), eq(SEARCH_REQUEST))).thenReturn(EMPTY_SEARCH_RESULT);
        when(externalSearchOutbound.externalServiceSearch(eq(CLIENT_PKB), eq(SEARCH_REQUEST))).thenReturn(NON_EMPTY_SEARCH_RESULT);
        when(externalSearchOutbound.externalServiceSearch(eq(CLIENT_CFT), eq(SEARCH_REQUEST))).thenThrow(NOT_FOUND_EXCEPTION);

        String actualResult = internalSearchService.internalSearch(SEARCH_REQUEST);

        assertEquals(FOUND_CLIENT_ID, actualResult);
    }

    @Test
    void internalSearch_nonNotFoundErrorWithoutData_throwsFailedPrecondition() {
        when(externalSearchOutbound.externalServiceSearch(eq(CLIENT_MDM), eq(SEARCH_REQUEST))).thenReturn(EMPTY_SEARCH_RESULT);
        when(externalSearchOutbound.externalServiceSearch(eq(CLIENT_PKB), eq(SEARCH_REQUEST))).thenThrow(NOT_FOUND_EXCEPTION);
        when(externalSearchOutbound.externalServiceSearch(eq(CLIENT_CFT), eq(SEARCH_REQUEST))).thenThrow(SERVICE_UNAVAILABLE_EXCEPTION);

        assertThrows(SearchFailedPreconditionException.class, () -> internalSearchService.internalSearch(SEARCH_REQUEST));
    }
}
