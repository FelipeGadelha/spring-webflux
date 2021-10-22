package br.com.felipe.gadelha.webflux.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Table(value = "animes")
public class Anime {

    @Id
    private Long id;
    @NotNull
    @NotEmpty(message = "The name of this anime cannot be empty")
    private String name;

    @Deprecated
    private Anime() { }

    private Anime(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
    }
    public Long getId() { return id; }
    public String getName() { return name; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long id;
        private String name;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }

        public Anime build() { return new Anime(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Anime anime = (Anime) o;
        return Objects.equals(id, anime.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Anime{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
