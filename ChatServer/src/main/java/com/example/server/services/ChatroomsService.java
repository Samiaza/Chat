package com.example.server.services;

import com.example.server.models.entities.Chatroom;

import java.util.List;

public interface ChatroomsService {
    void save(Chatroom room);
    List<Chatroom> getAll();
    String getNameById(Long id);
    Long getCreatorIdByRoomId(Long id);
}
