package com.example.server.repositories;

import com.example.server.models.entities.User;

import java.util.Optional;

public interface UsersRepository extends CrudRepository<User> {
    Optional<User> findByLogin(String login);
}
