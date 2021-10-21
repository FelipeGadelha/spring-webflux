package br.com.felipe.gadelha.webflux.util;

import br.com.felipe.gadelha.webflux.domain.entity.Anime;

public class AnimeBuilder {

    public static Anime builderAnimeToBeSaved(){
        return Anime.builder()
                .name("Tensei Shitara Slime Datta ken")
                .build();
    }

    public static Anime builderAnimeValid(){
        return Anime.builder()
                .id(1L)
                .name("Tensei Shitara Slime Datta ken")
                .build();
    }
}
