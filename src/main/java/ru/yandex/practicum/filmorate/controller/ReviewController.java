package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review create(@RequestBody Review review) {
        log.info("Создание отзыва: {}", review);
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@RequestBody Review review) {
        log.info("Обновление отзыва: {}", review);
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.info("Удаление отзыва с ID={}", id);
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public Review getById(@PathVariable int id) {
        log.info("Получение отзыва с ID={}", id);
        return reviewService.getById(id);
    }

    @GetMapping
    public List<Review> getReviewsByFilmId(
            @RequestParam(required = false) Integer filmId,
            @RequestParam(defaultValue = "10") int count) {

        log.info("Получение отзывов для filmId={}, count={}", filmId, count);
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Добавление лайка отзыву {} от пользователя {}", id, userId);
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable int id, @PathVariable int userId) {
        log.info("Добавление дизлайка отзыву {} от пользователя {}", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Удаление лайка отзыва {} пользователем {}", id, userId);
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable int id, @PathVariable int userId) {
        log.info("Удаление дизлайка отзыва {} пользователем {}", id, userId);
        reviewService.removeDislike(id, userId);
    }
}