package com.example.server.services;

import com.example.server.repositories.MessagesRepository;
import com.example.server.models.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component("messagesService")
public class MessagesServiceImpl implements MessagesService {
    private final MessagesRepository messagesRepository;

    @Autowired
    public MessagesServiceImpl(MessagesRepository messagesRepository) {
        this.messagesRepository = messagesRepository;
    }

    @Override
    public void save(Message message) {
        messagesRepository.save(message);
    }

    @Override
    public List<Message> getLastMessages(Long roomId, Long count) {
        List<Message> allMessages = messagesRepository.findAll();
        return allMessages.stream().filter(m -> m.getRoom().equals(roomId))
                .sorted(Comparator.comparing(Message::getDateTime).reversed())
                .limit(count).collect(Collectors.toList());
    }
}
