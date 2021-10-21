package br.com.felipe.gadelha.webflux.api.controller;

import br.com.felipe.gadelha.webflux.domain.entity.Anime;
import br.com.felipe.gadelha.webflux.domain.service.AnimeService;
import br.com.felipe.gadelha.webflux.util.AnimeBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
class AnimeControllerTest {

    @InjectMocks
    private AnimeController animeController;

    @Mock
    private AnimeService animeServiceMock;

    private final Anime anime = AnimeBuilder.builderAnimeValid();

    @BeforeAll
    public static void blockHoundSetup() { BlockHound.install(); }

    @BeforeEach
    public void setUp() {
        BDDMockito.when(animeServiceMock.findAll())
                .thenReturn(Flux.just(anime));
        BDDMockito.when(animeServiceMock.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.just(anime));
        BDDMockito.when(animeServiceMock.save(AnimeBuilder.builderAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));
        BDDMockito.when(animeServiceMock.saveAll(List.of(AnimeBuilder.builderAnimeToBeSaved(), AnimeBuilder.builderAnimeToBeSaved())))
                .thenReturn(Flux.just(anime, anime));
        BDDMockito.when(animeServiceMock.delete(ArgumentMatchers.anyLong()))
                .thenReturn(Mono.empty());
        BDDMockito.when(animeServiceMock.update(AnimeBuilder.builderAnimeValid()))
                .thenReturn(Mono.empty());

    }

    @Test
    public void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);
            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    @DisplayName("should findAll anime successful")
    public void test1() {
        StepVerifier.create(animeController.findAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("should findById anime successful")
    public void test2() {
        StepVerifier.create(animeController.findById(1L))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("should save anime successful")
    public void test4() {
        var animeToBeSaved = AnimeBuilder.builderAnimeToBeSaved();
        StepVerifier.create(animeController.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("should saveAll animes successful")
    public void test5() {
        var animeToBeSaved = AnimeBuilder.builderAnimeToBeSaved();
        StepVerifier.create(animeController.saveBatch(List.of(animeToBeSaved, animeToBeSaved)))
                .expectSubscription()
                .expectNext(anime, anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("should delete anime successful")
    public void test6() {
        StepVerifier.create(animeController.delete(1L))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("should update anime successful")
    public void test7() {
        StepVerifier.create(animeController.update(1L, AnimeBuilder.builderAnimeValid()))
                .expectSubscription()
                .verifyComplete();
    }
}