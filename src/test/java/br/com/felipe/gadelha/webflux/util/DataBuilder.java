package br.com.felipe.gadelha.webflux.util;

import br.com.felipe.gadelha.webflux.domain.entity.User;
import br.com.felipe.gadelha.webflux.domain.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@Component
public class DataBuilder {

    @Autowired
    private UserRepository repository;
    @Autowired
    private WebTestClient webClient;
    @Autowired
    private JacksonParse jackson;

    private String typeToken = "Bearer ";

    private BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();

    public void userDataBuilder() {
        String encode = encrypt("123456");
        var user = User.builder()
                .name("user")
                .username("user@email.com")
                .password(encode)
                .authorities("ROLE_USER")
                .build();
        repository.save(user).subscribe();
    }

    public void adminDataBuilder() {
        String encode = encrypt("123456");
        var admin = User.builder()
                .name("admin")
                .username("admin@email.com")
                .password(encode)
                .authorities("ROLE_USER, ROLE_ADMIN")
                .build();
        repository.save(admin).subscribe();
    }

    public String getToken(TokenAuth tokenAuth) {

        WebTestClient.ResponseSpec responseSpec = webClient.post()
                .uri("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(tokenAuth.getTokenRq()))
                .exchange()
                .expectStatus()
                .isOk();

        return responseSpec.returnResult(String.class).getResponseBody()
                .map(response -> {
                    String token = jackson.toMap(response).get("access_token").toString();
                    return typeToken + token;
                }).next()
                .block();
    }

    private String encrypt(String password) {
        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder()
                .encode(password);
    }

}