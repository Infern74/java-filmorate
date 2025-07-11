package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmController(FilmService filmService, MpaStorage mpaStorage,
                          GenreStorage genreStorage) {
        this.filmService = filmService;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Запрос на создание фильма: {}", film);
        validateFilm(film);
        validateMpa(film.getMpa().getId());
        validateGenres(film.getGenres());
        Film createdFilm = filmService.create(film);
        log.info("Создан новый фильм: {}", createdFilm);
        return createdFilm;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Запрос на обновление фильма: {}", film);
        validateFilm(film);
        validateMpa(film.getMpa().getId());
        validateGenres(film.getGenres());
        Film updatedFilm = filmService.update(film);
        log.info("Обновлен фильм: {}", updatedFilm);
        return updatedFilm;
    }

    @GetMapping
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable int id) {
        return filmService.getById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Запрос на добавление лайка фильму {} от пользователя {}", id, userId);
        filmService.addLike(id, userId);
        log.info("Пользователь {} добавил лайк фильму {}", userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, id);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        log.info("Запрос {} популярных фильмов, genreId: {}, year: {}", count, genreId, year);
        return filmService.getPopularFilms(count, genreId, year);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Название фильма не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Описание превышает 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null) {
            log.error("Дата релиза не указана");
            throw new ValidationException("Дата релиза обязательна");
        }
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.error("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.error("Некорректная продолжительность: {}", film.getDuration());
            throw new ValidationException("Продолжительность должна быть положительной");
        }
    }

    private void validateMpa(int mpaId) {
        try {
            mpaStorage.getById(mpaId);
        } catch (MpaNotFoundException e) {
            throw new MpaNotFoundException("Рейтинг MPA с id=" + mpaId + " не найден");
        }
    }

    private void validateGenres(Set<Genre> genres) {
        if (genres != null) {
            for (Genre genre : genres) {
                try {
                    genreStorage.getById(genre.getId());
                } catch (GenreNotFoundException e) {
                    throw new GenreNotFoundException("Жанр с id=" + genre.getId() + " не найден");
                }
            }
        }
    }
}