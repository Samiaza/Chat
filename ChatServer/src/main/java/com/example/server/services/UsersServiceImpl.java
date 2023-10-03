package com.example.server.services;

import com.example.server.exceptions.EntityAlreadyExistsException;
import com.example.server.exceptions.EntityNotFoundException;
import com.example.server.repositories.UsersRepository;
import com.example.server.models.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component("usersService")
public class UsersServiceImpl implements UsersService{
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersServiceImpl(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void signUp(String login, String password) {
        User user = usersRepository.findByLogin(login).orElse(null);
        if (user != null) {
            throw new EntityAlreadyExistsException("User with given name already exists");
        }
        user = new User(null, login, passwordEncoder.encode(password));
        usersRepository.save(user);
    }

    @Override
    public Long getIdByLogin(String login) {
        User user = usersRepository.findByLogin(login).orElse(null);
        return user != null ? user.getId() : null;
    }

    @Override
    public String getLoginById(Long id) {
        User user = usersRepository.findById(id).orElse(null);
        return user != null ? user.getLogin() : null;
    }

    @Override
    public boolean signIn(String login, String password) {
        User user = usersRepository.findByLogin(login).orElse(null);
        if (user == null) {
            throw new EntityNotFoundException("User with given name is not found");
        }
        return passwordEncoder.matches(password, user.getPassword());
    }
}
