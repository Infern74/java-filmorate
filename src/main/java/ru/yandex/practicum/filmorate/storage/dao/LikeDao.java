package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class LikeDao {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Film> filmRowMapper;

    public LikeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = (rs, rowNum) -> Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(MpaRating.builder()
                        .id(rs.getInt("mpa_rating_id"))
                        .name(rs.getString("mpa_name"))
                        .build())
                .genres(Collections.emptySet())
                .build();
    }

    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, m.name AS mpa_name, " +
                "fg.genre_id, g.name AS genre_name, " +
                "COUNT(l.user_id) AS likes_count " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                "LEFT JOIN genres g ON fg.genre_id = g.id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id, fg.genre_id, g.id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";

        List<FilmWithGenre> filmWithGenres = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new FilmWithGenre(
                        filmRowMapper.mapRow(rs, rowNum),
                        rs.getInt("genre_id"),
                        rs.getString("genre_name")
                ),
                count
        );

        Map<Integer, Set<Genre>> genresByFilmId = filmWithGenres.stream()
                .filter(fwg -> fwg.genreId != 0)
                .collect(Collectors.groupingBy(
                        fwg -> fwg.film.getId(),
                        Collectors.mapping(
                                fwg -> Genre.builder().id(fwg.genreId).name(fwg.genreName).build(),
                                Collectors.toSet()
                        )
                ));

        return filmWithGenres.stream()
                .map(fwg -> fwg.film)
                .distinct()
                .map(film -> {
                    Set<Genre> genres = genresByFilmId.getOrDefault(film.getId(), Collections.emptySet());
                    return film.toBuilder().genres(genres).build();
                })
                .collect(Collectors.toList());
    }

    private static class FilmWithGenre {
        final Film film;
        final int genreId;
        final String genreName;

        FilmWithGenre(Film film, int genreId, String genreName) {
            this.film = film;
            this.genreId = genreId;
            this.genreName = genreName;
        }
    }
}