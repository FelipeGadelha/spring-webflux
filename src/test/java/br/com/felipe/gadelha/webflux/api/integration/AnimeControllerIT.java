package br.com.felipe.gadelha.webflux.api.integration;

import br.com.felipe.gadelha.webflux.domain.entity.Anime;
import br.com.felipe.gadelha.webflux.domain.repository.AnimeRepository;
import br.com.felipe.gadelha.webflux.util.AnimeBuilder;
import br.com.felipe.gadelha.webflux.util.DataBuilder;
import br.com.felipe.gadelha.webflux.util.TokenAuth;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
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

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@ActiveProfiles(value = "test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
public class AnimeControllerIT {

    final String BASE_PATH = "/animes";

    @Autowired
    private AnimeRepository animeRepository;

    private final Anime anime = AnimeBuilder.builderAnimeToBeSaved();

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private DataBuilder dataBuilder;

//    @Autowired
//    public void setup(ApplicationContext context) {
//        this.webClient = WebTestClient
//                .bindToApplicationContext(context)
//                .apply(springSecurity())
//                .configureClient()
//                .build();
//    }

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
                "CREATE TABLE animes ( id SERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL);",
                "DROP TABLE IF EXISTS users ;",
                "CREATE TABLE users (\n" +
                        "\tid serial not null,\n" +
                        "\tname varchar not null,\n" +
                        "\tusername varchar(100) not null,\n" +
                        "\tpassword varchar not null,\n" +
                        "\tauthorities varchar(150) not null,\n" +
                        "\t\n" +
                        "    UNIQUE(username),\n" +
                        "    CONSTRAINT users_pk PRIMARY KEY (id)\n" +
                        ");");
        statements.forEach(it -> databaseClient.sql(it)
                .fetch()
                .rowsUpdated()
                .block());
        dataBuilder.adminDataBuilder();
        dataBuilder.userDataBuilder();
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
    public void test1() throws Exception {
        animeRepository.save(anime).subscribe();

        webClient.get()
                .uri(BASE_PATH)
                .header("Authorization", dataBuilder.getToken(TokenAuth.ADMIN))
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
                .header("Authorization", dataBuilder.getToken(TokenAuth.ADMIN))
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
                .header("Authorization", dataBuilder.getToken(TokenAuth.USER))
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
                .header("Authorization", dataBuilder.getToken(TokenAuth.USER))
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
                .header("Authorization", dataBuilder.getToken(TokenAuth.ADMIN))
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
                .header("Authorization", dataBuilder.getToken(TokenAuth.ADMIN))
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
                .header("Authorization", dataBuilder.getToken(TokenAuth.ADMIN))
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
                .header("Authorization", dataBuilder.getToken(TokenAuth.ADMIN))
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
                .header("Authorization", dataBuilder.getToken(TokenAuth.ADMIN))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("not should delete anime when anime does not exists in database")
    public void test10() {
        webClient.delete()
                .uri(BASE_PATH + "/{id}", 1L)
                .header("Authorization", dataBuilder.getToken(TokenAuth.ADMIN))
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
                .header("Authorization", dataBuilder.getToken(TokenAuth.ADMIN))
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
                .header("Authorization", dataBuilder.getToken(TokenAuth.ADMIN))
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
