package br.com.felipe.gadelha.webflux.api.dto.response;

import br.com.felipe.gadelha.webflux.domain.entity.User;
import reactor.core.publisher.Mono;

public class UserRs {

    private final Long id;
    private final String username;
    private final String name;
    public UserRs(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.username = user.getUsername();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
}
