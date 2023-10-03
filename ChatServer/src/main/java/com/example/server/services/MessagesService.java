package com.example.server.services;

import com.example.server.models.entities.Message;

import java.util.List;

public interface MessagesService {
    void save(Message message);
    List<Message> getLastMessages(Long roomId, Long count);
}
