package com.example.server.models.messages;

public enum ClientStatus {
    OUT_OF_SERVER,
    JOINED_TO_SERVER,
    ENTERING_NEW_LOGIN,
    ENTERING_NEW_PASSWORD,
    ENTERING_EXISTS_LOGIN,
    ENTERING_EXISTS_PASSWORD,
//    WAITING_FOR_LOGIN,
    LOGGED_ON,
    CREATING_ROOM,
    CHOOSING_ROOM,
    WAITING_FOR_JOIN_TO_ROOM,
    JOINED_TO_ROOM
}
