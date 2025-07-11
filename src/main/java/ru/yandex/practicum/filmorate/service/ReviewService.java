package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Review create(Review review) {
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        return reviewStorage.update(review);
    }

    public void delete(int id) {
        reviewStorage.delete(id);
    }

    public Review getById(int id) {
        return reviewStorage.getById(id);
    }

    public List<Review> getReviewsByFilmId(Integer filmId, int count) {
        filmStorage.getById(filmId);
        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    public void addLike(int reviewId, int userId) {
        validateReviewAndUser(reviewId, userId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        validateReviewAndUser(reviewId, userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(int reviewId, int userId) {
        validateReviewAndUser(reviewId, userId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(int reviewId, int userId) {
        validateReviewAndUser(reviewId, userId);
        reviewStorage.removeDislike(reviewId, userId);
    }

    private void validateUserAndFilm(int userId, int filmId) {
        userStorage.getById(userId);
        filmStorage.getById(filmId);
    }

    private void validateReviewAndUser(int reviewId, int userId) {
        reviewStorage.getById(reviewId);
        userStorage.getById(userId);
    }
}