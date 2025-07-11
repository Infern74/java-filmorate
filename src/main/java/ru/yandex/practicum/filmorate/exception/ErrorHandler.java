package ru.yandex.practicum.filmorate.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException e) {
        log.warn("Ошибка валидации: {}", e.getMessage());
        return new ErrorResponse("Ошибка валидации: " + e.getMessage());
    }

    @ExceptionHandler({FilmNotFoundException.class, UserNotFoundException.class,
            GenreNotFoundException.class, MpaNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final RuntimeException e) {
        log.warn("Объект не найден: {}", e.getMessage());
        return new ErrorResponse("Объект не найден: " + e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOtherExceptions(final Throwable e) {
        log.error("Внутренняя ошибка сервера", e);
        return new ErrorResponse("Произошла непредвиденная ошибка: " + e.getMessage());
    }

    @ExceptionHandler(LikeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleLikeNotFoundException(final LikeNotFoundException e) {
        log.warn("Лайк не найден: {}", e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleReviewNotFoundException(final ReviewNotFoundException e) {
        log.warn("Отзыв не найден: {}", e.getMessage());
        return new ErrorResponse("Отзыв не найден: " + e.getMessage());
    }

    @Data
    @AllArgsConstructor
    static class ErrorResponse {
        private String error;
    }

    @ExceptionHandler(DirectorNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDirectorNotFound(DirectorNotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

}