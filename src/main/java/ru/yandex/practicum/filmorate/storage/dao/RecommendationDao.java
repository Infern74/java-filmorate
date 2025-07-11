package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class RecommendationDao {
    private final JdbcTemplate jdbcTemplate;

    public Map<Integer, Set<Integer>> getUserLikes() {
        String sql = "SELECT user_id, film_id FROM likes";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        Map<Integer, Set<Integer>> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer userId = (Integer) row.get("user_id");
            Integer filmId = (Integer) row.get("film_id");
            result.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
        }
        return result;
        
    }
}

