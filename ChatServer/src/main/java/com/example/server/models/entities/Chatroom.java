package com.example.server.models.entities;

import java.util.Objects;
import java.util.Set;

public class Chatroom {
    private Long id;
    private String name;
    private Long creatorId;
    private Set<User> users;
    private Set<Message> messages;


    public Chatroom() {
    }

    public Chatroom(Long id, String name, Long creatorId, Set<User> users, Set<Message> messages) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.users = users;
        this.messages = messages;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCreator(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getCreator() {
        return creatorId;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }

    public Set<Message> getMessages() {
        return messages;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Chatroom guest = (Chatroom) obj;
        return Objects.equals(id, guest.id) && name.equals(guest.name) && creatorId.equals(guest.creatorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, creatorId);
    }

    @Override
    public String toString() {
        return "Chatroom{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", creatorId=" + creatorId +
                ", users=" + users +
                ", messages=" + messages +
                '}';
    }
}
