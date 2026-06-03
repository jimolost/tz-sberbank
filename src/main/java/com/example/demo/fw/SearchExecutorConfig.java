package com.example.demo.fw;

import com.example.demo.app.api.search.SearchBdIndexes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Пул потоков для параллельных вызовов
 */
@Configuration
public class SearchExecutorConfig {
    @Bean
    Executor searchExecutor() {
        return Executors.newFixedThreadPool(SearchBdIndexes.ALL.size());
    }
}
