package com.github.bohnman.squiggly.spring;

import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class SquigglyWebFilterTest {

    @Test
    void filterAddsQueryParamToReactorContext() {
        SquigglyWebFilter webFilter = new SquigglyWebFilter("fields");
        MockServerHttpRequest request = MockServerHttpRequest.get("/api?fields=name,age").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = ex -> Mono.deferContextual(ctx -> {
            assertEquals("name,age", ctx.get(SquigglyWebFilter.CONTEXT_KEY));
            return Mono.empty();
        });

        StepVerifier.create(webFilter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filterDoesNotModifyContextWhenParamAbsent() {
        SquigglyWebFilter webFilter = new SquigglyWebFilter("fields");
        MockServerHttpRequest request = MockServerHttpRequest.get("/api").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = ex -> Mono.deferContextual(ctx -> {
            assertFalse(ctx.hasKey(SquigglyWebFilter.CONTEXT_KEY));
            return Mono.empty();
        });

        StepVerifier.create(webFilter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filterUsesConfiguredParameterName() {
        SquigglyWebFilter webFilter = new SquigglyWebFilter("filter");
        MockServerHttpRequest request = MockServerHttpRequest.get("/api?filter=id").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = ex -> Mono.deferContextual(ctx -> {
            assertEquals("id", ctx.get(SquigglyWebFilter.CONTEXT_KEY));
            return Mono.empty();
        });

        StepVerifier.create(webFilter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void orderIsHighestPrecedencePlusOne() {
        SquigglyWebFilter webFilter = new SquigglyWebFilter("fields");
        assertEquals(Ordered.HIGHEST_PRECEDENCE + 1, webFilter.getOrder());
    }

    @Test
    void chainIsAlwaysInvoked() {
        SquigglyWebFilter webFilter = new SquigglyWebFilter("fields");

        // With param
        AtomicBoolean chainInvokedWithParam = new AtomicBoolean(false);
        MockServerHttpRequest requestWith = MockServerHttpRequest.get("/api?fields=name").build();
        MockServerWebExchange exchangeWith = MockServerWebExchange.from(requestWith);
        WebFilterChain chainWith = ex -> {
            chainInvokedWithParam.set(true);
            return Mono.empty();
        };
        StepVerifier.create(webFilter.filter(exchangeWith, chainWith)).verifyComplete();
        assertTrue(chainInvokedWithParam.get());

        // Without param
        AtomicBoolean chainInvokedWithout = new AtomicBoolean(false);
        MockServerHttpRequest requestWithout = MockServerHttpRequest.get("/api").build();
        MockServerWebExchange exchangeWithout = MockServerWebExchange.from(requestWithout);
        WebFilterChain chainWithout = ex -> {
            chainInvokedWithout.set(true);
            return Mono.empty();
        };
        StepVerifier.create(webFilter.filter(exchangeWithout, chainWithout)).verifyComplete();
        assertTrue(chainInvokedWithout.get());
    }
}
