package br.com.felipe.gadelha.webflux.api.controller;

import br.com.felipe.gadelha.webflux.api.dto.request.UserRq;
import br.com.felipe.gadelha.webflux.api.dto.response.UserRs;
import br.com.felipe.gadelha.webflux.domain.entity.User;
import br.com.felipe.gadelha.webflux.domain.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<?>> save(@Valid @RequestBody UserRq userRq) {
        User user = userRq.convert();
        return userService.existsByUsername(user.getUsername())
                .map(exixts -> (exixts)
                        ? ResponseEntity.badRequest().body("User AlRead Exists")
                        : ResponseEntity.ok(userService.save(user).map(UserRs::new))
                );
    }
}
