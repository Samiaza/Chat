package com.example.server.models.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Message {
    private Long id;
    private Long authorId;
    private Long roomId;
    private String text;
    private LocalDateTime dateTime;

    public Message() {
    }

    public Message(Long id, Long authorId, Long roomId, String text, LocalDateTime dateTime) {
        this.id = id;
        this.authorId = authorId;
        this.roomId = roomId;
        this.text = text;
        this.dateTime = dateTime;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setAuthor(Long authorId) {
        this.authorId = authorId;
    }

    public Long getAuthor() {
        return authorId;
    }

    public void setRoom(Long roomId) {
        this.roomId = roomId;
    }

    public Long getRoom() {
        return roomId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Message guest = (Message) obj;
        return Objects.equals(id, guest.id) && authorId.equals(guest.authorId) && roomId.equals(guest.roomId)
                && text.equals(guest.text) && dateTime.equals(guest.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, authorId, roomId, text, dateTime);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", authorId=" + authorId +
                ", roomId=" + roomId +
                ", text='" + text + '\'' +
                ", dateTime=" + dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")) +
                '}';
    }
}