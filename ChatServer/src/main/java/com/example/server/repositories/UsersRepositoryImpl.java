package com.example.server.repositories;

import com.example.server.models.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

@Component("usersRepository")
public class UsersRepositoryImpl implements UsersRepository {
    private final RowMapper<User> ROW_MAPPER = (ResultSet rs, int rowNum) ->
            new User(rs.getLong("id"), rs.getString("login"), rs.getString("password"));
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UsersRepositoryImpl(DataSource ds) {
        jdbcTemplate = new JdbcTemplate(ds);
    }

    @Override
    public Optional<User> findById(Long id) {
        String sqlString = String.format("SELECT * FROM users WHERE id = %s", id);
        List<User> users = jdbcTemplate.query(sqlString, ROW_MAPPER);
        return Optional.ofNullable(users.size() > 0 ? users.get(0) : null);
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", ROW_MAPPER);
    }

    @Override
    public void save(User user) {
        jdbcTemplate.update("INSERT INTO users (login, password) VALUES (?, ?)", user.getLogin(), user.getPassword());
    }

    @Override
    public void update(User user) {
        jdbcTemplate.update("UPDATE users SET login = ?, password = ? WHERE id = ?", user.getLogin(), user.getPassword(), user.getId());
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }


    @Override
    public Optional<User> findByLogin(String login) {
        String sqlString = String.format("SELECT * FROM users WHERE login = '%s'", login);
        List<User> users = jdbcTemplate.query(sqlString, ROW_MAPPER);
        return Optional.ofNullable(users.size() > 0 ? users.get(0) : null);
    }
}
