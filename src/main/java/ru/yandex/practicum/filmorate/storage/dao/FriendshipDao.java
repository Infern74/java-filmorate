package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.util.List;

@Repository
public class FriendshipDao {
    private final JdbcTemplate jdbcTemplate;

    public FriendshipDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addFriend(int userId, int friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, FriendshipStatus.PENDING.name());
    }

    public void confirmFriend(int userId, int friendId) {
        String sql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, FriendshipStatus.CONFIRMED.name(), userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    public List<Integer> getFriends(int userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, Integer.class, userId);
    }

    public FriendshipStatus getFriendshipStatus(int userId, int friendId) {
        String sql = "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?";
        try {
            return FriendshipStatus.valueOf(
                    jdbcTemplate.queryForObject(sql, String.class, userId, friendId)
            );
        } catch (Exception e) {
            return null;
        }
    }
}