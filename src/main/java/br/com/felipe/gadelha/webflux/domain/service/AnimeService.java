package br.com.felipe.gadelha.webflux.domain.service;

import br.com.felipe.gadelha.webflux.domain.entity.Anime;
import br.com.felipe.gadelha.webflux.domain.repository.AnimeRepository;
import io.netty.util.internal.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AnimeService {

    private final AnimeRepository animeRepository;

    public AnimeService(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }

    public Flux<Anime> findAll() {
        return animeRepository.findAll();
    }

    public Mono<Anime> findById(Long id) {
        return animeRepository.findById(id)
                .switchIfEmpty(monoResponseStatusNotFoundException());
    }

    public Mono<Anime> save(Anime anime) { return animeRepository.save(anime); }

    public Mono<Void> update(Anime anime) {
        return this.findById(anime.getId())
                .flatMap(validAnime -> animeRepository.save(anime))
                .then();
//                .thenEmpty(Mono.empty());
    }

    public Mono<Void> delete(Long id) {
        return this.findById(id)
                .flatMap(animeRepository::delete);
    }


    private <T> Mono<T> monoResponseStatusNotFoundException() {
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
    }

    @Transactional
    public Flux<Anime> saveAll(List<Anime> animes) {
        return animeRepository.saveAll(animes)
                .doOnNext(this::throwResponseStatusExceptionWhenEmptyName);
    }

    private void throwResponseStatusExceptionWhenEmptyName(Anime anime) {
        if (StringUtil.isNullOrEmpty(anime.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Name");
    }
}
