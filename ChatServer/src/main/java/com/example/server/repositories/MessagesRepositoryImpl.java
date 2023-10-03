package com.example.server.repositories;

import com.example.server.models.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

@Component("messagesRepository")
public class MessagesRepositoryImpl implements MessagesRepository {
    private final RowMapper<Message> ROW_MAPPER = (ResultSet rs, int rowNum) ->
            new Message(rs.getLong("id"), rs.getLong("author"), rs.getLong("room"),
                    rs.getString("text"), rs.getTimestamp("datetime").toLocalDateTime());
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MessagesRepositoryImpl(DataSource ds) {
        jdbcTemplate = new JdbcTemplate(ds);
    }

    @Override
    public Optional<Message> findById(Long id) {
        String sqlString = String.format("SELECT * FROM messages WHERE id = %s", id);
        List<Message> messages = jdbcTemplate.query(sqlString, ROW_MAPPER);
        return Optional.ofNullable(messages.size() > 0 ? messages.get(0) : null);
    }

    @Override
    public List<Message> findAll() {
        return jdbcTemplate.query("SELECT * FROM messages", ROW_MAPPER);
    }

    @Override
    public void save(Message mess) {
        jdbcTemplate.update("INSERT INTO messages (author, room, text) VALUES (?, ?, ?)", mess.getAuthor(), mess.getRoom(), mess.getText());
    }

    @Override
    public void update(Message mess) {
        jdbcTemplate.update("UPDATE messages SET text = ? WHERE id = ?", mess.getText(),  mess.getId());
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM messages WHERE id = ?", id);
    }

}
