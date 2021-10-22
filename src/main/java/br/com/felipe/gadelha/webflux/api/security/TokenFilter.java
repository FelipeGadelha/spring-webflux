package br.com.felipe.gadelha.webflux.api.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Objects;
@Component
public class TokenFilter implements WebFilter {

    private TokenProvider tokenProvider;
    public static final String HEADER_PREFIX = "Bearer ";

    public TokenFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = retrieveToken(exchange.getRequest());
        System.err.println(token);
        if (StringUtils.hasText(token) && this.tokenProvider.isValidToken(token)) {
            Authentication authentication = this.tokenProvider.getAuthentication(token);
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        }
        return chain.filter(exchange);
    }

    private String retrieveToken(ServerHttpRequest request) {
        String token = request.getHeaders (). getFirst (HttpHeaders.AUTHORIZATION);
        if (Objects.isNull(token) || token.isEmpty() || !token.startsWith(HEADER_PREFIX)) return null;
        return token.substring(7);
    }
}
