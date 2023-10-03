package com.example.client.models;

import java.util.List;
import java.util.Map;

public class MessageFromServer {
    public String author;
    public String message;
    public Map<Integer, String> menuItems;
    public List<MessageFromServer> messagesHistory;
    public Long attachedId;
    public Long attacheRoomId;
}
