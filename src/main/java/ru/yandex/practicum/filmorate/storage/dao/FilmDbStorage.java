package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
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
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreDbStorage genreDbStorage, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreDbStorage = genreDbStorage;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            updateFilmDirectors(film);
        }

        return getById(id);
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

        jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ?", film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            updateFilmDirectors(film);
        }

        return getById(film.getId());
    }

    @Override
    public Film delete(int id) {
        Film film = getById(id);
        deleteFilm(id);
        return film;
    }

    @Override
    public void deleteFilm(int id) {
        String sql = "DELETE FROM films WHERE id = ?";
        int deleted = jdbcTemplate.update(sql, id);
        if (deleted == 0) {
            throw new FilmNotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    @Override
    public Collection<Film> getAll() {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);

        if (!films.isEmpty()) {
            loadGenresForFilms(films);
            loadDirectorsForFilms(films);
        }

        return films;
    }

    @Override
    public Film getById(int id) {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
            loadGenresForFilms(List.of(film));
            loadDirectorsForFilms(List.of(film));
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new FilmNotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    @Override
    public List<Film> getByIds(Set<Integer> ids) {
        if (ids.isEmpty()) return List.of();
        String sql = "SELECT f.*, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "WHERE f.id IN (:ids)";
        Map<String, Object> params = Map.of("ids", ids);
        List<Film> films = namedParameterJdbcTemplate.query(sql, params, this::mapRowToFilm);

        if (!films.isEmpty()) {
            loadGenresForFilms(films);
            loadDirectorsForFilms(films);
        }

        return films;
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByYear(int directorId) {
        String sql = "SELECT f.*, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "JOIN film_directors fd ON f.id = fd.film_id " +
                "WHERE fd.director_id = ? " +
                "ORDER BY f.release_date";

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, directorId);

        if (!films.isEmpty()) {
            loadGenresForFilms(films);
            loadDirectorsForFilms(films);
        }

        return films;
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByLikes(int directorId) {
        String sql = "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "JOIN film_directors fd ON f.id = fd.film_id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "WHERE fd.director_id = ? " +
                "GROUP BY f.id " +
                "ORDER BY likes_count DESC";

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, directorId);

        if (!films.isEmpty()) {
            loadGenresForFilms(films);
            loadDirectorsForFilms(films);
        }

        return films;
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sql = "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "JOIN likes l ON f.id = l.film_id " +
                "WHERE f.id IN ( " +
                "    SELECT film_id FROM likes WHERE user_id = ? " +
                "    INTERSECT " +
                "    SELECT film_id FROM likes WHERE user_id = ? " +
                ") " +
                "GROUP BY f.id, m.name " +
                "ORDER BY likes_count DESC";

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, userId, friendId);

        if (!films.isEmpty()) {
            loadGenresForFilms(films);
            loadDirectorsForFilms(films);
        }

        return films;
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
        loadDirectorsForFilms(films);
        return films;
    }

    @Override
    public List<Film> searchFilms(String query, boolean searchByTitle, boolean searchByDirector) {
        String sql = "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN likes l ON f.id = l.film_id ";

        if (searchByDirector) {
            sql += "LEFT JOIN film_directors fd ON f.id = fd.film_id " +
                    "LEFT JOIN directors d ON fd.director_id = d.id ";
        }

        sql += "WHERE ";

        List<String> conditions = new ArrayList<>();
        if (searchByTitle) {
            conditions.add("LOWER(f.name) LIKE LOWER(?)");
        }
        if (searchByDirector) {
            conditions.add("LOWER(d.name) LIKE LOWER(?)");
        }

        sql += String.join(" OR ", conditions);
        sql += " GROUP BY f.id, m.name ORDER BY likes_count DESC";

        String searchPattern = "%" + query + "%";

        List<Film> films = jdbcTemplate.query(sql,
                ps -> {
                    int index = 1;
                    if (searchByTitle) {
                        ps.setString(index++, searchPattern);
                    }
                    if (searchByDirector) {
                        ps.setString(index, searchPattern);
                    }
                },
                this::mapRowToFilm
        );

        if (!films.isEmpty()) {
            loadGenresForFilms(films);
            loadDirectorsForFilms(films);
        }

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
                .directors(new HashSet<>())
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

    private void updateFilmDirectors(Film film) {
        List<Object[]> batchArgs = film.getDirectors().stream()
                .map(director -> new Object[]{film.getId(), director.getId()})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(
                "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
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
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

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
                genresByFilmId.computeIfAbsent(filmId, k -> new LinkedHashSet<>())
                        .add(genre);
            }
        }

        return genresByFilmId;
    }

    private void loadDirectorsForFilms(List<Film> films) {
        if (films == null || films.isEmpty()) return;

        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        if (filmIds.isEmpty()) {
            return;
        }

        String inClause = String.join(",", Collections.nCopies(filmIds.size(), "?"));

        String sql = "SELECT fd.film_id, d.id, d.name " +
                "FROM film_directors fd " +
                "JOIN directors d ON fd.director_id = d.id " +
                "WHERE fd.film_id IN (" + inClause + ")";

        Map<Integer, Set<Director>> directorsByFilmId = jdbcTemplate.query(sql, rs -> {
            Map<Integer, Set<Director>> result = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                Director director = Director.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .build();
                result.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
            }
            return result;
        }, filmIds.toArray());

        films.forEach(film -> {
            Set<Director> directors = directorsByFilmId.get(film.getId());
            film.setDirectors(directors != null ? directors : new HashSet<>());
        });
    }
}