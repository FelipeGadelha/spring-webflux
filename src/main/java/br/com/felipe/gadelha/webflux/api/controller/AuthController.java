package br.com.felipe.gadelha.webflux.api.controller;


import br.com.felipe.gadelha.webflux.api.dto.request.TokenRq;
import br.com.felipe.gadelha.webflux.api.security.TokenProvider;
import br.com.felipe.gadelha.webflux.domain.service.UserDetailsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final TokenProvider tokenProvider;

    private final ReactiveAuthenticationManager authenticationManager;

    public AuthController(TokenProvider tokenProvider, ReactiveAuthenticationManager authenticationManager) {
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping
    public Mono<ResponseEntity> login(@Valid @RequestBody Mono<TokenRq> tokenRq) {
        return tokenRq.flatMap(loginData -> this.authenticationManager
                    .authenticate(loginData.convertToAuthentication())
                    .map(this.tokenProvider::generateToken)
                ).map(token -> new ResponseEntity<>(Map.of("type", "Bearer", "access_token", token), HttpStatus.OK));
    }


}
