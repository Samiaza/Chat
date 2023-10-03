package com.example.server.repositories;

import com.example.server.models.entities.Chatroom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

@Component("chatroomsRepository")
public class ChatroomsRepositoryImpl implements ChatroomsRepository {
    private final RowMapper<Chatroom> ROW_MAPPER = (ResultSet rs, int rowNum) ->
            new Chatroom(rs.getLong("id"), rs.getString("name"), rs.getLong("creator"),
                    null, null);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ChatroomsRepositoryImpl(DataSource ds) {
        jdbcTemplate = new JdbcTemplate(ds);
    }

    @Override
    public Optional<Chatroom> findById(Long id) {
        String sqlString = String.format("SELECT * FROM chatrooms WHERE id = %s", id);
        List<Chatroom> users = jdbcTemplate.query(sqlString, ROW_MAPPER);
        return Optional.ofNullable(users.size() > 0 ? users.get(0) : null);
    }

    @Override
    public List<Chatroom> findAll() {
        return jdbcTemplate.query("SELECT * FROM chatrooms", ROW_MAPPER);
    }

    @Override
    public void save(Chatroom room) {
        jdbcTemplate.update("INSERT INTO chatrooms (name, creator) VALUES (?, ?)", room.getName(), room.getCreator());
    }

    @Override
    public void update(Chatroom room) {
        jdbcTemplate.update("UPDATE chatrooms SET name = ? WHERE id = ?", room.getName(), room.getId());
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM chatrooms WHERE id = ?", id);
    }

}
