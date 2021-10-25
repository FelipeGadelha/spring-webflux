package br.com.felipe.gadelha.webflux.domain.service;

import br.com.felipe.gadelha.webflux.domain.entity.User;
import br.com.felipe.gadelha.webflux.domain.repository.UserRepository;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public Mono<Boolean> existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    public Mono<User> save(User user) { return userRepository.save(user); }



}
