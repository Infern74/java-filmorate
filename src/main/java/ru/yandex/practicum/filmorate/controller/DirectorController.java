package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
@Slf4j
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getAll() {
        log.info("Получен запрос всех режиссеров");
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable int id) {
        log.info("Получен запрос режиссера с id={}", id);
        return directorService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director create(@RequestBody Director director) {
        log.info("Получен запрос на создание режиссера: {}", director);
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@RequestBody Director director) {
        log.info("Получен запрос на обновление режиссера: {}", director);
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        log.info("Получен запрос на удаление режиссера с id={}", id);
        directorService.delete(id);
    }
}