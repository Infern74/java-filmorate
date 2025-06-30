package ru.yandex.practicum.filmorate.exception;

public class MpaNotFoundException extends EntityNotFoundException {
    public MpaNotFoundException(String message) {
        super(message);
    }
}