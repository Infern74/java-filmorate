package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreDbStorage genreDbStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreDbStorage genreDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreDbStorage = genreDbStorage;
    }

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");
        int id = simpleJdbcInsert.executeAndReturnKey(filmToMap(film)).intValue();
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updateFilmGenres(film);
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        if (rowsUpdated == 0) {
            throw new FilmNotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updateFilmGenres(film);
        }

        return film;
    }

    @Override
    public Film delete(int id) {
        Film film = getById(id);
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
        return film;
    }

    @Override
    public Collection<Film> getAll() {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);

        if (films.isEmpty()) {
            return films;
        }

        loadGenresForFilms(films);

        return films;
    }

    @Override
    public Film getById(int id) {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);

            loadGenresForFilms(Collections.singletonList(film));

            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new FilmNotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    @Override
    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        String sql = "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                (genreId != null ? "JOIN film_genres fg ON f.id = fg.film_id AND fg.genre_id = ? " : "") +
                (year != null ? "WHERE EXTRACT(YEAR FROM f.release_date) = ? " : "") +
                "GROUP BY f.id, m.name " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            int paramIndex = 1;
            if (genreId != null) {
                ps.setInt(paramIndex++, genreId);
            }
            if (year != null) {
                ps.setInt(paramIndex++, year);
            }
            ps.setInt(paramIndex, count);
            return ps;
        }, this::mapRowToFilm);

        loadGenresForFilms(films);
        return films;
    }

    private Map<String, Object> filmToMap(Film film) {
        return Map.of(
                "name", film.getName(),
                "description", film.getDescription(),
                "release_date", film.getReleaseDate(),
                "duration", film.getDuration(),
                "mpa_rating_id", film.getMpa().getId()
        );
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        MpaRating mpa = MpaRating.builder()
                .id(rs.getInt("mpa_rating_id"))
                .name(rs.getString("mpa_name"))
                .build();

        return Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpa)
                .genres(new HashSet<>())
                .build();
    }

    private void updateFilmGenres(Film film) {
        List<Object[]> batchArgs = new ArrayList<>();
        for (Genre genre : film.getGenres()) {
            batchArgs.add(new Object[]{film.getId(), genre.getId()});
        }
        jdbcTemplate.batchUpdate(
                "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                batchArgs
        );
    }

    private void loadGenresForFilms(List<Film> films) {
        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Integer, Set<Genre>> genresByFilmId = loadGenresByFilmIds(filmIds);

        films.forEach(film ->
                film.setGenres(genresByFilmId.getOrDefault(film.getId(), Collections.emptySet()))
        );
    }

    private Map<Integer, Set<Genre>> loadGenresByFilmIds(List<Integer> filmIds) {
        String inClause = filmIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String sql = "SELECT fg.film_id, fg.genre_id " +
                "FROM film_genres fg " +
                "WHERE fg.film_id IN (" + inClause + ")";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        Map<Integer, Set<Genre>> genresByFilmId = new HashMap<>();

        Map<Integer, Genre> allGenres = genreDbStorage.getAll().stream()
                .collect(Collectors.toMap(Genre::getId, Function.identity()));

        for (Map<String, Object> row : rows) {
            Integer filmId = (Integer) row.get("film_id");
            Integer genreId = (Integer) row.get("genre_id");

            Genre genre = allGenres.get(genreId);
            if (genre != null) {
                genresByFilmId.computeIfAbsent(filmId, k -> new HashSet<>())
                        .add(genre);
            }
        }

        return genresByFilmId;
    }
}