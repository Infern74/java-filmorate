package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public List<Genre> getAllGenres() {
        log.info("Запрос на получение списка всех жанров");
        List<Genre> genres = genreService.getAllGenres();
        log.info("Получено {} жанров", genres.size());
        return genres;
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable int id) {
        log.info("Запрос на получение жанра с id={}", id);
        Genre genre = genreService.getGenreById(id);
        log.info("Найден жанр: {} (id={})", genre.getName(), genre.getId());
        return genre;
    }
}