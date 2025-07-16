package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.dao.RecommendationDao;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationDao recommendationDao;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> getRecommendations(int userId) {
        userStorage.getById(userId);

        Map<Integer, Set<Integer>> userLikes = recommendationDao.getUserLikes();
        Set<Integer> targetLikes = userLikes.getOrDefault(userId, Collections.emptySet());

        if (targetLikes.isEmpty()) {
            return Collections.emptyList();
        }

        // найдём похожих пользователей с пересечением лайков
        List<Integer> similarUsers = userLikes.entrySet().stream()
                .filter(entry -> entry.getKey() != userId)
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), overlap(targetLikes, entry.getValue())))
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(10) // ограничение на количество похожих пользователей
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (similarUsers.isEmpty()) {
            return Collections.emptyList();
        }

        // собираем рекомендации от всех похожих пользователей
        Set<Integer> recommendedFilmIds = similarUsers.stream()
                .map(userLikes::get)
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .filter(filmId -> !targetLikes.contains(filmId)) // исключаем уже лайкнутые
                .collect(Collectors.toSet());

        if (recommendedFilmIds.isEmpty()) {
            return Collections.emptyList();
        }

        // загружаем фильмы списком
        return filmStorage.getByIds(recommendedFilmIds);
    }

    private int overlap(Set<Integer> a, Set<Integer> b) {
        Set<Integer> copy = new HashSet<>(a);
        copy.retainAll(b);
        return copy.size();
    }
}

