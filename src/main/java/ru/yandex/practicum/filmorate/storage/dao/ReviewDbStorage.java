package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review create(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");

        int reviewId = simpleJdbcInsert.executeAndReturnKey(reviewToMap(review)).intValue();
        review.setReviewId(reviewId);
        review.setUseful(0);
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        if (rowsUpdated == 0) {
            throw new ReviewNotFoundException("Отзыв с ID=" + review.getReviewId() + " не найден");
        }
        return review;
    }

    @Override
    public Review delete(int id) {
        Review review = getById(id);
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, id);
        return review;
    }

    @Override
    public Review getById(int id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToReview, id);
        } catch (Exception e) {
            throw new ReviewNotFoundException("Отзыв с id=" + id + " не найден");
        }
    }

    @Override
    public List<Review> getReviewsByFilmId(Integer filmId, int count) {
        String sql;
        if (filmId == null) {
            sql = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
            return jdbcTemplate.query(sql, this::mapRowToReview, count);
        } else {
            sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
            return jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
        }
    }

    @Override
    public void addLike(int reviewId, int userId) {
        Boolean current = getCurrentLikeStatus(reviewId, userId);
        int delta;

        if (current == null) {
            jdbcTemplate.update(
                    "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, true)",
                    reviewId, userId
            );
            delta = 1;
        } else if (current) {
            return;
        } else {
            jdbcTemplate.update(
                    "UPDATE review_likes SET is_like = true WHERE review_id = ? AND user_id = ?",
                    reviewId, userId
            );
            delta = 2;
        }
        updateReviewUseful(reviewId, delta);
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        Boolean current = getCurrentLikeStatus(reviewId, userId);
        int delta;

        if (current == null) {
            jdbcTemplate.update(
                    "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, false)",
                    reviewId, userId
            );
            delta = -1;
        } else if (!current) {
            return;
        } else {
            jdbcTemplate.update(
                    "UPDATE review_likes SET is_like = false WHERE review_id = ? AND user_id = ?",
                    reviewId, userId
            );
            delta = -2;
        }
        updateReviewUseful(reviewId, delta);
    }

    @Override
    public void removeLike(int reviewId, int userId) {
        Boolean current = getCurrentLikeStatus(reviewId, userId);
        if (current != null && current) {
            jdbcTemplate.update(
                    "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?",
                    reviewId, userId
            );
            updateReviewUseful(reviewId, -1);
        }
    }

    @Override
    public void removeDislike(int reviewId, int userId) {
        Boolean current = getCurrentLikeStatus(reviewId, userId);
        if (current != null && !current) {
            jdbcTemplate.update(
                    "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?",
                    reviewId, userId
            );
            updateReviewUseful(reviewId, 1);
        }
    }

    private Map<String, Object> reviewToMap(Review review) {
        return Map.of(
                "content", review.getContent(),
                "is_positive", review.getIsPositive(),
                "user_id", review.getUserId(),
                "film_id", review.getFilmId(),
                "useful", 0
        );
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(rs.getInt("review_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getInt("user_id"))
                .filmId(rs.getInt("film_id"))
                .useful(rs.getInt("useful"))
                .build();
    }

    private void updateReviewUseful(int reviewId, int delta) {
        jdbcTemplate.update(
                "UPDATE reviews SET useful = useful + ? WHERE review_id = ?",
                delta, reviewId
        );
    }

    private Boolean getCurrentLikeStatus(int reviewId, int userId) {
        String sql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Boolean.class, reviewId, userId);
        } catch (Exception e) {
            return null;
        }
    }
}