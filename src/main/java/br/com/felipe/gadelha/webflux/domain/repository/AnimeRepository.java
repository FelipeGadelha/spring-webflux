package br.com.felipe.gadelha.webflux.domain.repository;

import br.com.felipe.gadelha.webflux.domain.entity.Anime;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface AnimeRepository extends R2dbcRepository<Anime, Long> {

    Mono<Anime> findById(Long id);

}
