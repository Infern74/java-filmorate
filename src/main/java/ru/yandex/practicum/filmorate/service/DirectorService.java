package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<Director> getAll() {
        return directorStorage.getAll();
    }

    public Director getById(int id) {
        return directorStorage.getById(id);
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {

        // Проверяем существование режиссера перед обновлением
        getById(director.getId()); // Если режиссер не найден, выбросит DirectorNotFoundException
        return directorStorage.update(director);
    }

    public void delete(int id) {

        // Проверяем существование режиссера перед удалением
        getById(id); // Если режиссер не найден, выбросит DirectorNotFoundException
        directorStorage.delete(id);
    }
}