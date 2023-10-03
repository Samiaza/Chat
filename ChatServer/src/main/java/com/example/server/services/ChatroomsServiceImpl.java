package com.example.server.services;

import com.example.server.repositories.ChatroomsRepository;
import com.example.server.models.entities.Chatroom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("chatroomsService")
public class ChatroomsServiceImpl implements ChatroomsService {
    private final ChatroomsRepository chatroomsRepository;

    @Autowired
    public ChatroomsServiceImpl(ChatroomsRepository chatroomsRepository) {
        this.chatroomsRepository = chatroomsRepository;
    }

    @Override
    public void save(Chatroom room) {
        chatroomsRepository.save(room);
    }

    @Override
    public List<Chatroom> getAll() {
        return chatroomsRepository.findAll();
    }

    @Override
    public String getNameById(Long id) {
        Chatroom room = chatroomsRepository.findById(id).orElse(null);
        return room != null ? room.getName() : null;
    }

    @Override
    public Long getCreatorIdByRoomId(Long id) {
        Chatroom room = chatroomsRepository.findById(id).orElse(null);
        return room != null ? room.getCreator() : null;
    }


}
