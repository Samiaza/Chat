package com.example.server.services;

public interface UsersService {
    boolean signIn(String login, String password);
    void signUp(String login, String password);
    Long getIdByLogin(String login);
    String getLoginById(Long id);
}
