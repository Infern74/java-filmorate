package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.storage.dao.LikeDao;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final LikeDao likeDao;
    private final EventLogger eventLogger;
    private final DirectorService directorService;


    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        return filmStorage.getById(id);
    }


    public void addLike(int filmId, int userId) {
        getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        likeDao.addLike(filmId, userId);
        eventLogger.log(userId, EventType.LIKE, OperationType.ADD, filmId);
    }

    public void removeLike(int filmId, int userId) {
        getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        likeDao.removeLike(filmId, userId);
        eventLogger.log(userId, EventType.LIKE, OperationType.REMOVE, filmId);
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть положительным числом");
        }

        if (genreId != null && genreId <= 0) {
            throw new ValidationException("ID жанра должно быть положительным числом");
        }

        if (year != null && year < 1895) {
            throw new ValidationException("Год не может быть раньше 1895");
        }
        return filmStorage.getPopularFilms(count, genreId, year);
    }

    private void getFilmOrThrow(int id) {
        filmStorage.getById(id);
    }

    private void getUserOrThrow(int id) {
        userStorage.getById(id);
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        directorService.getById(directorId);

        if (sortBy.equals("year")) {
            return filmStorage.getFilmsByDirectorSortedByYear(directorId);
        } else {
            return filmStorage.getFilmsByDirectorSortedByLikes(directorId);
        }
    }

    public List<Film> searchFilms(String query, String[] by) {
        Set<String> searchParams = new HashSet<>(Arrays.asList(by));
        boolean searchByTitle = searchParams.contains("title");
        boolean searchByDirector = searchParams.contains("director");

        if (!searchByTitle && !searchByDirector) {
            throw new ValidationException("Параметр 'by' должен содержать 'title' и/или 'director'");
        }

        return filmStorage.searchFilms(query, searchByTitle, searchByDirector);
    }

    public void delete(int id) {
        getFilmOrThrow(id);
        filmStorage.deleteFilm(id);
    }

    @Deprecated
    public Film deleteAndReturn(int id) {
        Film film = filmStorage.getById(id);
        filmStorage.deleteFilm(id);
        return film;
    }

}