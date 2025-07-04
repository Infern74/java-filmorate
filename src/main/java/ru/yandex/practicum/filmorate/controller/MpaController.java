package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    public List<MpaRating> getAllMpaRatings() {
        log.info("Запрос на получение списка всех рейтингов MPA");
        List<MpaRating> ratings = mpaService.getAllMpaRatings();
        log.info("Получено {} рейтингов MPA", ratings.size());
        return ratings;
    }

    @GetMapping("/{id}")
    public MpaRating getMpaRatingById(@PathVariable int id) {
        log.info("Запрос на получение рейтинга MPA с id={}", id);
        MpaRating rating = mpaService.getMpaRatingById(id);
        log.info("Найден рейтинг MPA: {} (id={})", rating.getName(), rating.getId());
        return rating;
    }
}