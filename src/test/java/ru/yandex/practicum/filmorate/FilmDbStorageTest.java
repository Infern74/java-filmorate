package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.MpaDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        MpaRating mpa = mpaStorage.getById(1);
        Genre genre = genreStorage.getById(1);

        testFilm = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .genres(Collections.singleton(genre))
                .build();
    }

    @Test
    void createFilm_ShouldReturnFilmWithId() {
        Film createdFilm = filmStorage.create(testFilm);

        assertThat(createdFilm.getId()).isPositive();
        assertThat(createdFilm.getName()).isEqualTo("Test Film");
        assertThat(createdFilm.getGenres()).hasSize(1);
    }

    @Test
    void updateFilm_ShouldUpdateFields() {
        Film createdFilm = filmStorage.create(testFilm);
        Film updatedFilm = createdFilm.toBuilder()
                .name("Updated Film")
                .description("Updated Description")
                .build();

        Film result = filmStorage.update(updatedFilm);

        assertThat(result.getName()).isEqualTo("Updated Film");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void getById_ShouldReturnCorrectFilm() {
        Film createdFilm = filmStorage.create(testFilm);
        Film foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm).isEqualTo(createdFilm);
        assertThat(foundFilm.getGenres()).hasSize(1);
    }

    @Test
    void getById_ShouldThrowExceptionForNonExistingFilm() {
        assertThatThrownBy(() -> filmStorage.getById(999))
                .isInstanceOf(FilmNotFoundException.class)
                .hasMessageContaining("Фильм с id=999 не найден");
    }

    @Test
    void getAll_ShouldReturnAllFilms() {
        Film film1 = filmStorage.create(testFilm);
        Film film2 = filmStorage.create(testFilm.toBuilder().name("Another Film").build());

        Collection<Film> films = filmStorage.getAll();

        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getName)
                .containsExactlyInAnyOrder("Test Film", "Another Film");
    }

    @Test
    void updateFilm_ShouldUpdateGenres() {
        Film createdFilm = filmStorage.create(testFilm);
        Genre newGenre = genreStorage.getById(2);

        Film updatedFilm = createdFilm.toBuilder()
                .genres(Set.of(newGenre))
                .build();

        Film result = filmStorage.update(updatedFilm);

        assertThat(result.getGenres()).hasSize(1);
        assertThat(result.getGenres()).extracting(Genre::getId).containsExactly(2);
    }

    @Test
    void deleteFilm_ShouldRemoveFilm() {
        Film createdFilm = filmStorage.create(testFilm);
        filmStorage.delete(createdFilm.getId());

        assertThatThrownBy(() -> filmStorage.getById(createdFilm.getId()))
                .isInstanceOf(FilmNotFoundException.class);
    }
}