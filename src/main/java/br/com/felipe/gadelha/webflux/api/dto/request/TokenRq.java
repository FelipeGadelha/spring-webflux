package br.com.felipe.gadelha.webflux.api.dto.request;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.validation.constraints.NotBlank;

public class TokenRq {

    @NotBlank
    private String username;
    @NotBlank
    private String password;

    public TokenRq(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UsernamePasswordAuthenticationToken convertToAuthentication() {
        return new UsernamePasswordAuthenticationToken(username, password);
    }
}