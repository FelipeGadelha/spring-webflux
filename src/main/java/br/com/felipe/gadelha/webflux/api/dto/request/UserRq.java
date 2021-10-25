package br.com.felipe.gadelha.webflux.api.dto.request;

import br.com.felipe.gadelha.webflux.domain.entity.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

public class UserRq {

    private String name;
    private String username;
    private String password;

    public UserRq(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public User convert() {
        return User.builder()
                .name(name)
                .username(username)
                .password(encrypt(password))
                .authorities("ROLE_USER")
                .build();
    }

    private String encrypt(String password) {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password);
    }
}
