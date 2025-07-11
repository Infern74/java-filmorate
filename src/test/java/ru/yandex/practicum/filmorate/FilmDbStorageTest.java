package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.dao.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class, DirectorDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;
    private final DirectorDbStorage directorStorage;

    private Film testFilm;
    private Director testDirector;

    @BeforeEach
    void setUp() {
        MpaRating mpa = mpaStorage.getById(1);
        Genre genre = genreStorage.getById(1);
        testDirector = directorStorage.create(Director.builder().name("Test Director").build());

        testFilm = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .genres(Collections.singleton(genre))
                .directors(Set.of(testDirector))
                .build();
    }

    @Test
    void createFilm_ShouldReturnFilmWithIdAndDirectors() {
        Film createdFilm = filmStorage.create(testFilm);

        assertThat(createdFilm.getId()).isPositive();
        assertThat(createdFilm.getName()).isEqualTo("Test Film");
        assertThat(createdFilm.getGenres()).hasSize(1);
        assertThat(createdFilm.getDirectors())
                .hasSize(1)
                .extracting(Director::getId)
                .containsExactly(testDirector.getId());
    }

    @Test
    void updateFilm_ShouldUpdateFieldsAndDirectors() {
        Film createdFilm = filmStorage.create(testFilm);
        Director newDirector = directorStorage.create(Director.builder().name("New Director").build());

        Film updatedFilm = createdFilm.toBuilder()
                .name("Updated Film")
                .description("Updated Description")
                .directors(Set.of(newDirector))
                .build();

        Film result = filmStorage.update(updatedFilm);

        assertThat(result.getName()).isEqualTo("Updated Film");
        assertThat(result.getDirectors())
                .hasSize(1)
                .extracting(Director::getId)
                .containsExactly(newDirector.getId());
    }

    @Test
    void getById_ShouldReturnFilmWithDirectors() {
        Film createdFilm = filmStorage.create(testFilm);
        Film foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm.getDirectors())
                .hasSize(1)
                .extracting(Director::getId)
                .containsExactly(testDirector.getId());
    }

    @Test
    void getAll_ShouldReturnFilmsWithDirectors() {
        Film film1 = filmStorage.create(testFilm);
        Film film2 = filmStorage.create(testFilm.toBuilder()
                .name("Another Film")
                .directors(Collections.emptySet())
                .build());

        Collection<Film> films = filmStorage.getAll();

        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getDirectors)
                .containsExactlyInAnyOrder(
                        Set.of(testDirector),
                        Collections.emptySet()
                );
    }

    @Test
    void updateFilm_ShouldUpdateDirectors() {
        Film createdFilm = filmStorage.create(testFilm);
        Director newDirector = directorStorage.create(Director.builder().name("New Director").build());

        Film updatedFilm = createdFilm.toBuilder()
                .directors(Set.of(newDirector))
                .build();

        Film result = filmStorage.update(updatedFilm);

        assertThat(result.getDirectors())
                .hasSize(1)
                .extracting(Director::getId)
                .containsExactly(newDirector.getId());
    }

    @Test
    void getFilmsByDirectorSortedByYear_ShouldReturnOrderedFilms() {
        // Создаём тестовые фильмы с одним режиссёром, но разными годами
        Film film2000 = Film.builder()
                .name("Film 2000")
                .description("Старый фильм")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpaStorage.getById(1))
                .directors(Set.of(testDirector))
                .build();

        Film film2010 = Film.builder()
                .name("Film 2010")
                .description("Новый фильм")
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(90)
                .mpa(mpaStorage.getById(1))
                .directors(Set.of(testDirector))
                .build();

        filmStorage.create(film2000);
        filmStorage.create(film2010);

        List<Film> result = filmStorage.getFilmsByDirectorSortedByYear(testDirector.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Film 2000");
        assertThat(result.get(1).getName()).isEqualTo("Film 2010");
    }

}