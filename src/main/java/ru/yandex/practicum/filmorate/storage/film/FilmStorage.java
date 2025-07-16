package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    Film delete(int id);

    Collection<Film> getAll();

    Film getById(int id);

    public List<Film> getByIds(Set<Integer> ids);

    List<Film> getFilmsByDirectorSortedByYear(int directorId);

    List<Film> getFilmsByDirectorSortedByLikes(int directorId);

    List<Film> getPopularFilms(int count, Integer genreId, Integer year);
}