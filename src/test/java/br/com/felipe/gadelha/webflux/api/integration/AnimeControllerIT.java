package br.com.felipe.gadelha.webflux.api.integration;

import br.com.felipe.gadelha.webflux.domain.entity.Anime;
import br.com.felipe.gadelha.webflux.domain.repository.AnimeRepository;
import br.com.felipe.gadelha.webflux.util.AnimeBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ActiveProfiles(value = "test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AnimeControllerIT {

    final String BASE_PATH = "/animes";

    @Autowired
    private AnimeRepository animeRepository;

    private final Anime anime = AnimeBuilder.builderAnimeToBeSaved();

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private DatabaseClient databaseClient;


//    private List<Anime> getData(){
//        return Arrays.asList(
//                Anime.builder().name("Naruto").build(),
//                Anime.builder().name("Boruto").build(),
//                Anime.builder().name("Jujutsu Kaisen").build(),
//                Anime.builder().name("Doctor Stone").build(),
//                Anime.builder().name("Cells at Work").build(),
//                Anime.builder().name("One Punch-Man").build(),
//                Anime.builder().name("One Piece").build()
//        );
//    }

    @BeforeEach
    public void setup(){

        List<String> statements = Arrays.asList(
                "DROP TABLE IF EXISTS animes ;",
                "CREATE TABLE animes ( id SERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL);");
        System.err.println("---------------------------------------------------------------------------");
        statements.forEach(it -> databaseClient.sql(it)
                .fetch()
                .rowsUpdated()
                .block());

//        System.err.println("---------------------------------------------------------------------------");
//        animeRepository.deleteAll()
//                .thenMany(Flux.fromIterable(getData()))
//                .flatMap(animeRepository::save)
//                .doOnNext(anime ->{
//                    System.out.println("User Inserted from AnimeControllerTest: " + anime);
//                })
//                .blockLast();
//        System.err.println("---------------------------------------------------------------------------");
    }

    @BeforeAll
    public static void blockHoundSetup() { BlockHound.install(); }

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
        animeRepository.save(anime).subscribe();

        webClient.get()
                .uri(BASE_PATH)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(anime.getId())
                .jsonPath("$.[0].name").isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("should findAll anime successful")
    public void test2() {
        animeRepository.save(anime).subscribe();
        webClient.get()
                .uri(BASE_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);
    }

    @Test
    @DisplayName("should findById anime successful")
    public void test3() {
        animeRepository.save(anime).subscribe();
        webClient.get()
                .uri(BASE_PATH+ "/{id}", anime.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("not should findById anime when anime does not exists")
    public void test4() {
        webClient.get()
                .uri(BASE_PATH+ "/{id}", 1L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("should save anime successfully")
    public void test5() {
        Anime animeToBeSaved = AnimeBuilder.builderAnimeToBeSaved();
        webClient.post()
                .uri(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo(animeToBeSaved.getName());
    }

    @Test
    @DisplayName("should saveAll a list anime successfully")
    public void test6() {
        Anime naruto = Anime.builder().name("Naruto").build();
        Anime one_piece = Anime.builder().name("One Piece").build();
        webClient.post()
                .uri(BASE_PATH + "/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(naruto, one_piece)))
                .exchange()
                .expectStatus().isCreated();

        StepVerifier.create(animeRepository.findAll())
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
    }
    @Test
    @DisplayName("not should saveAll a list anime when contains anime with null or empty name")
    public void test7() {
        Anime empty = Anime.builder().name("").build();
        Anime one_piece = Anime.builder().name("One Piece").build();
        webClient.post()
                .uri(BASE_PATH + "/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(one_piece, empty)))
                .exchange()
                .expectStatus().isBadRequest();

        StepVerifier.create(animeRepository.findAll())
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("not should save anime when anime with name empty")
    public void test8() {
        Anime anime = Anime.builder().name("").build();
        webClient.post()
                .uri(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("should delete anime successfully")
    public void test9() {
        animeRepository.save(anime).subscribe();
        webClient.delete()
                .uri(BASE_PATH + "/{id}", anime.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("not should delete anime when anime does not exists in database")
    public void test10() {
        webClient.delete()
                .uri(BASE_PATH + "/{id}", 1L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("should update anime successfully")
    public void test11() {
        animeRepository.save(anime).subscribe();
        Anime animeUpdate = Anime.builder()
                .name("updated cowboy bebop")
                .build();
        webClient.put()
                .uri(BASE_PATH + "/{id}", anime.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeUpdate)
                ).exchange()
                .expectStatus().isNoContent();

        StepVerifier.create(animeRepository.findById(anime.getId()))
                .expectSubscription()
                .expectNext(Anime.builder()
                        .id(anime.getId())
                        .name(animeUpdate.getName())
                        .build()
                ).verifyComplete();

    }

    @Test
    @DisplayName("not should update anime when anime not found in database")
    public void test12() {
        webClient.put()
                .uri(BASE_PATH + "/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(Anime.builder().name("Updated Naruto").build()))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");;

    }

//    @Test
//    public void expectInvalidNexts() {
//        Flux<String> flux = Flux.just("foo", "bar");
//
//        assertThatExceptionOfType(AssertionError.class)
//                .isThrownBy(() -> StepVerifier.create(flux)
//                        .expectNext("foo", "baz")
//                        .expectComplete()
//                        .verify())
//                .withMessage("expectation \"expectNext(baz)\" failed (expected value: baz; actual value: bar)");
//    }
}
