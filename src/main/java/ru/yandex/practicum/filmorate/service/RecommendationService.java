package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.dao.RecommendationDao;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationDao recommendationDao;
    private final FilmStorage filmStorage;

    public List<Film> getRecommendations(int userId) {
        Map<Integer, Set<Integer>> userLikes = recommendationDao.getUserLikes();

        Set<Integer> targetLikes = userLikes.getOrDefault(userId, Collections.emptySet());

        int bestMatchUserId = -1;
        int maxOverlap = 0;

        for (Map.Entry<Integer, Set<Integer>> entry : userLikes.entrySet()) {
            if (entry.getKey() == userId) continue;

            Set<Integer> otherLikes = entry.getValue();

            Set<Integer> intersection = new HashSet<>(targetLikes);
            intersection.retainAll(otherLikes);
            int overlap = intersection.size();

            if (overlap > maxOverlap) {
                maxOverlap = overlap;
                bestMatchUserId = entry.getKey();
            }
        }

        if (bestMatchUserId == -1) {
            return Collections.emptyList();
        }

        Set<Integer> bestMatchLikes = userLikes.get(bestMatchUserId);

        Set<Integer> recommendations = new HashSet<>(bestMatchLikes);
        recommendations.removeAll(targetLikes);

        return recommendations.stream()
                .map(filmStorage::getById)
                .collect(Collectors.toList());
    }
}

