package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController controller;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid Description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        validFilm.setName("");
        assertThrows(ValidationException.class, () -> controller.create(validFilm));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() {
        validFilm.setDescription("a".repeat(201));
        assertThrows(ValidationException.class, () -> controller.create(validFilm));
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateTooEarly() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> controller.create(validFilm));
    }

    @Test
    void shouldThrowExceptionWhenDurationNegative() {
        validFilm.setDuration(-10);
        assertThrows(ValidationException.class, () -> controller.create(validFilm));
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithEmptyName() {
        Film created = controller.create(validFilm);
        created.setName("");
        assertThrows(ValidationException.class, () -> controller.update(created));
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithLongDescription() {
        Film created = controller.create(validFilm);
        created.setDescription("a".repeat(201));
        assertThrows(ValidationException.class, () -> controller.update(created));
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithEarlyReleaseDate() {
        Film created = controller.create(validFilm);
        created.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> controller.update(created));
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithNegativeDuration() {
        Film created = controller.create(validFilm);
        created.setDuration(-10);
        assertThrows(ValidationException.class, () -> controller.update(created));
    }

    @Test
    void shouldReturnAllFilms() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(90);

        controller.create(film1);
        controller.create(film2);

        Collection<Film> allFilms = controller.getAll();

        assertAll(
                () -> assertEquals(2, allFilms.size()),
                () -> assertTrue(allFilms.contains(film1)),
                () -> assertTrue(allFilms.contains(film2))
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoFilms() {
        Collection<Film> allFilms = controller.getAll();
        assertTrue(allFilms.isEmpty());
    }
}