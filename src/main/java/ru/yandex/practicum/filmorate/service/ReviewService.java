package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    private final EventLogger eventLogger;

    public Review create(Review review) {
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        Review created = reviewStorage.create(review);
        eventLogger.log(created.getUserId(), EventType.REVIEW, OperationType.ADD, created.getReviewId());
        return getById(review.getReviewId());
    }

    public Review update(Review review) {

        Review updated = reviewStorage.update(review);
        eventLogger.log(getById(review.getReviewId()).getUserId(), EventType.REVIEW, OperationType.UPDATE, updated.getReviewId());
        return getById(review.getReviewId());
    }

    public void delete(int id) {
        Review deleted = getById(id);

        reviewStorage.delete(id);

        eventLogger.log(
                deleted.getUserId(),
                EventType.REVIEW,
                OperationType.REMOVE,
                id
        );
    }

    public Review getById(int id) {
        return reviewStorage.getById(id);
    }

    public List<Review> getReviewsByFilmId(Integer filmId, int count) {
        if (filmId != null) {
            filmStorage.getById(filmId);
        }
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