package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.dao.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.MpaDbStorage;

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
    private final JdbcTemplate jdbcTemplate;
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

    @Test
    void getPopularFilms_ShouldReturnTopFilmsFilteredByGenreAndYear() {
        Film film1 = filmStorage.create(testFilm);
        Film film2 = filmStorage.create(testFilm.toBuilder()
                .name("Less Popular Film")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .build());

        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user1@example.com", "user1", "User One", LocalDate.of(1990, 1, 1));
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user2@example.com", "user2", "User Two", LocalDate.of(1992, 2, 2));

        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", film1.getId(), 1);
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", film1.getId(), 2);
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", film2.getId(), 1);

        int genreId = testFilm.getGenres().iterator().next().getId();
        int year = testFilm.getReleaseDate().getYear();

        List<Film> popularFilms = filmStorage.getPopularFilms(10, genreId, year);

        assertThat(popularFilms).hasSize(2);
        assertThat(popularFilms.get(0).getId()).isEqualTo(film1.getId());
        assertThat(popularFilms.get(1).getId()).isEqualTo(film2.getId());
    }

    @Test
    void getCommonFilms_ShouldReturnCommonFilmsSortedByLikes() {

        Film film1 = filmStorage.create(testFilm.toBuilder().name("Film 1").build());
        Film film2 = filmStorage.create(testFilm.toBuilder().name("Film 2").build());
        Film film3 = filmStorage.create(testFilm.toBuilder().name("Film 3").build());

        jdbcTemplate.update("INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                1, "user1@example.com", "user1", "User One", LocalDate.of(1990, 1, 1));
        jdbcTemplate.update("INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                2, "user2@example.com", "user2", "User Two", LocalDate.of(1992, 2, 2));

        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", film1.getId(), 1);
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", film2.getId(), 1);
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", film2.getId(), 2);
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", film3.getId(), 2);

        List<Film> commonFilms = filmStorage.getCommonFilms(1, 2);

        assertThat(commonFilms)
                .hasSize(1)
                .extracting(Film::getName)
                .containsExactly("Film 2");
    }

    @Test
    void searchFilms_ShouldReturnFilmsByTitleAndDirector() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM film_directors");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM directors");
        jdbcTemplate.update("DELETE FROM users");

        MpaRating mpa = mpaStorage.getById(1);
        Genre genre = genreStorage.getById(1);

        Director director1 = directorStorage.create(Director.builder().name("Test Director").build());
        Director director2 = directorStorage.create(Director.builder().name("Great Director").build());

        Film film1 = filmStorage.create(Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .genres(Set.of(genre))
                .directors(Set.of(director1))
                .build());

        Film film2 = filmStorage.create(Film.builder()
                .name("Another Great Film")
                .description("Another Description")
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(90)
                .mpa(mpa)
                .genres(Set.of(genre))
                .directors(Collections.emptySet())
                .build());

        Film film3 = filmStorage.create(Film.builder()
                .name("Great Movie")
                .description("Great Description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(100)
                .mpa(mpa)
                .genres(Set.of(genre))
                .directors(Set.of(director2))
                .build());

        jdbcTemplate.update("INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                1, "user1@example.com", "user1", "User One", LocalDate.of(1990, 1, 1));
        jdbcTemplate.update("INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)",
                2, "user2@example.com", "user2", "User Two", LocalDate.of(1995, 1, 1));

        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", film1.getId(), 1);
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", film3.getId(), 1);
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", film3.getId(), 2);

        List<Film> byTitle = filmStorage.searchFilms("test", true, false);
        assertThat(byTitle)
                .hasSize(1)
                .extracting(Film::getName)
                .containsExactly("Test Film");

        List<Film> byDirector = filmStorage.searchFilms("great", false, true);
        assertThat(byDirector)
                .hasSize(1)
                .extracting(Film::getName)
                .containsExactly("Great Movie");

        List<Film> byBoth = filmStorage.searchFilms("great", true, true);
        assertThat(byBoth)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Another Great Film", "Great Movie");

        List<Film> sortedResults = filmStorage.searchFilms("great", true, true);
        assertThat(sortedResults)
                .extracting(Film::getName)
                .containsExactly("Great Movie", "Another Great Film");
    }
}