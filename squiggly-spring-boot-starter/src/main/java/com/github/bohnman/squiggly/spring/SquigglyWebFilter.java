package com.github.bohnman.squiggly.spring;

import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class SquigglyWebFilter implements WebFilter, Ordered {

    static final String CONTEXT_KEY = "squiggly.filter";

    private final String filterParameterName;

    public SquigglyWebFilter(String filterParameterName) {
        this.filterParameterName = filterParameterName;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String filter = exchange.getRequest().getQueryParams().getFirst(filterParameterName);

        if (filter != null) {
            return chain.filter(exchange)
                    .contextWrite(ctx -> ctx.put(CONTEXT_KEY, filter));
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
