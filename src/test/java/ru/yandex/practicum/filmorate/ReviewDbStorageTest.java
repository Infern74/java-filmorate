package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.dao.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({ReviewDbStorage.class, FilmDbStorage.class, UserDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ReviewDbStorageTest {

    private final ReviewDbStorage reviewStorage;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;
    private final JdbcTemplate jdbcTemplate;

    private User testUser;
    private Film testFilm;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("user@email.com")
                .login("user")
                .name("User Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        testUser = userStorage.create(testUser);

        MpaRating mpa = mpaStorage.getById(1);
        Genre genre = genreStorage.getById(1);

        testFilm = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .genres(Set.of(genre))
                .build();
        testFilm = filmStorage.create(testFilm);

        testReview = Review.builder()
                .content("This film is great!")
                .isPositive(true)
                .userId(testUser.getId())
                .filmId(testFilm.getId())
                .build();
    }

    @Test
    void createReview_ShouldReturnReviewWithIdAndUsefulZero() {
        Review createdReview = reviewStorage.create(testReview);

        assertThat(createdReview.getReviewId()).isPositive();
        assertThat(createdReview.getUseful()).isEqualTo(0);
        assertThat(createdReview.getContent()).isEqualTo("This film is great!");
    }

    @Test
    void updateReview_ShouldUpdateContent() {
        Review createdReview = reviewStorage.create(testReview);
        Review updatedReview = createdReview.toBuilder()
                .content("Updated content")
                .build();

        Review result = reviewStorage.update(updatedReview);

        assertThat(result.getContent()).isEqualTo("Updated content");
        assertThat(result.getReviewId()).isEqualTo(createdReview.getReviewId());
    }

    @Test
    void deleteReview_ShouldRemoveReview() {
        Review createdReview = reviewStorage.create(testReview);
        Review deletedReview = reviewStorage.delete(createdReview.getReviewId());

        assertThat(deletedReview).isEqualTo(createdReview);
        assertThatThrownBy(() -> reviewStorage.getById(createdReview.getReviewId()))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void getById_ShouldReturnCorrectReview() {
        Review createdReview = reviewStorage.create(testReview);
        Review foundReview = reviewStorage.getById(createdReview.getReviewId());

        assertThat(foundReview).isEqualTo(createdReview);
    }

    @Test
    void getById_ShouldThrowExceptionForNonExistingReview() {
        assertThatThrownBy(() -> reviewStorage.getById(999))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining("Отзыв с id=999 не найден");
    }

    @Test
    void getReviewsByFilmId_ShouldReturnReviewsForFilm() {
        Review createdReview = reviewStorage.create(testReview);

        // Создаем второй фильм и отзыв для него
        Film anotherFilm = testFilm.toBuilder().name("Another Film").build();
        anotherFilm = filmStorage.create(anotherFilm);
        Review anotherReview = testReview.toBuilder().filmId(anotherFilm.getId()).build();
        reviewStorage.create(anotherReview);

        List<Review> reviews = reviewStorage.getReviewsByFilmId(testFilm.getId(), 10);

        assertThat(reviews).hasSize(1);
        assertThat(reviews.getFirst()).isEqualTo(createdReview);
    }

    @Test
    void getReviewsWithoutFilmId_ShouldReturnAllReviews() {
        Review review1 = reviewStorage.create(testReview);
        Review review2 = reviewStorage.create(testReview.toBuilder().content("Second review").build());

        List<Review> reviews = reviewStorage.getReviewsByFilmId(null, 10);

        assertThat(reviews).hasSize(2);
        assertThat(reviews).extracting(Review::getReviewId)
                .containsExactlyInAnyOrder(review1.getReviewId(), review2.getReviewId());
    }

    @Test
    void addLike_ShouldIncreaseUseful() {
        Review createdReview = reviewStorage.create(testReview);
        reviewStorage.addLike(createdReview.getReviewId(), testUser.getId());

        Review updatedReview = reviewStorage.getById(createdReview.getReviewId());
        assertThat(updatedReview.getUseful()).isEqualTo(1);
    }

    @Test
    void addDislike_ShouldDecreaseUseful() {
        Review createdReview = reviewStorage.create(testReview);
        reviewStorage.addDislike(createdReview.getReviewId(), testUser.getId());

        Review updatedReview = reviewStorage.getById(createdReview.getReviewId());
        assertThat(updatedReview.getUseful()).isEqualTo(-1);
    }

    @Test
    void removeLike_ShouldDecreaseUseful() {
        Review createdReview = reviewStorage.create(testReview);
        reviewStorage.addLike(createdReview.getReviewId(), testUser.getId());
        reviewStorage.removeLike(createdReview.getReviewId(), testUser.getId());

        Review updatedReview = reviewStorage.getById(createdReview.getReviewId());
        assertThat(updatedReview.getUseful()).isEqualTo(0);
    }

    @Test
    void removeDislike_ShouldIncreaseUseful() {
        Review createdReview = reviewStorage.create(testReview);
        reviewStorage.addDislike(createdReview.getReviewId(), testUser.getId());
        reviewStorage.removeDislike(createdReview.getReviewId(), testUser.getId());

        Review updatedReview = reviewStorage.getById(createdReview.getReviewId());
        assertThat(updatedReview.getUseful()).isEqualTo(0);
    }

    @Test
    void changeLikeToDislike_ShouldUpdateUseful() {
        Review createdReview = reviewStorage.create(testReview);

        // Добавляем лайк
        reviewStorage.addLike(createdReview.getReviewId(), testUser.getId());
        Review afterLike = reviewStorage.getById(createdReview.getReviewId());
        assertThat(afterLike.getUseful()).isEqualTo(1);

        // Меняем на дизлайк
        reviewStorage.addDislike(createdReview.getReviewId(), testUser.getId());
        Review afterDislike = reviewStorage.getById(createdReview.getReviewId());
        assertThat(afterDislike.getUseful()).isEqualTo(-1);
    }

    @Test
    void changeDislikeToLike_ShouldUpdateUseful() {
        Review createdReview = reviewStorage.create(testReview);

        // Добавляем дизлайк
        reviewStorage.addDislike(createdReview.getReviewId(), testUser.getId());
        Review afterDislike = reviewStorage.getById(createdReview.getReviewId());
        assertThat(afterDislike.getUseful()).isEqualTo(-1);

        // Меняем на лайк
        reviewStorage.addLike(createdReview.getReviewId(), testUser.getId());
        Review afterLike = reviewStorage.getById(createdReview.getReviewId());
        assertThat(afterLike.getUseful()).isEqualTo(1);
    }

    @Test
    void multipleLikes_ShouldCalculateCorrectUseful() {
        // Создаем второго пользователя
        User user2 = userStorage.create(User.builder()
                .email("user2@email.com")
                .login("user2")
                .name("User 2")
                .birthday(LocalDate.of(1995, 5, 5))
                .build());

        Review createdReview = reviewStorage.create(testReview);

        // Первый пользователь ставит лайк
        reviewStorage.addLike(createdReview.getReviewId(), testUser.getId());

        // Второй пользователь ставит лайк
        reviewStorage.addLike(createdReview.getReviewId(), user2.getId());

        // Проверяем полезность
        Review updatedReview = reviewStorage.getById(createdReview.getReviewId());
        assertThat(updatedReview.getUseful()).isEqualTo(2);

        // Второй пользователь меняет на дизлайк
        reviewStorage.addDislike(createdReview.getReviewId(), user2.getId());

        // Проверяем полезность после изменения
        updatedReview = reviewStorage.getById(createdReview.getReviewId());
        assertThat(updatedReview.getUseful()).isEqualTo(0); // 1 (лайк) - 1 (дизлайк) = 0
    }

    @Test
    void getReviewsByFilmId_ShouldReturnOrderedByUseful() {
        // Создаем три отзыва с разной полезностью
        Review reviewLow = reviewStorage.create(testReview);
        Review reviewHigh = reviewStorage.create(testReview.toBuilder().content("High useful").build());
        Review reviewMedium = reviewStorage.create(testReview.toBuilder().content("Medium useful").build());

        // Устанавливаем полезность вручную
        jdbcTemplate.update("UPDATE reviews SET useful = ? WHERE review_id = ?", 10, reviewHigh.getReviewId());
        jdbcTemplate.update("UPDATE reviews SET useful = ? WHERE review_id = ?", 5, reviewMedium.getReviewId());
        jdbcTemplate.update("UPDATE reviews SET useful = ? WHERE review_id = ?", 1, reviewLow.getReviewId());

        List<Review> reviews = reviewStorage.getReviewsByFilmId(testFilm.getId(), 3);

        assertThat(reviews).extracting(Review::getReviewId)
                .containsExactly(
                        reviewHigh.getReviewId(),
                        reviewMedium.getReviewId(),
                        reviewLow.getReviewId()
                );
    }

    @Test
    void getReviewsByFilmId_ShouldRespectCountLimit() {
        // Создаем несколько отзывов
        for (int i = 0; i < 5; i++) {
            reviewStorage.create(testReview.toBuilder().content("Review " + i).build());
        }

        List<Review> reviews = reviewStorage.getReviewsByFilmId(testFilm.getId(), 3);
        assertThat(reviews).hasSize(3);
    }
}