package com.luciano.backend.usersapp.services;

import com.luciano.backend.usersapp.dto.UserDto;
import com.luciano.backend.usersapp.models.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDto> getAllUsers();
    Optional<UserDto> findById(long id);
    UserDto saveUser(User user);
    Optional<UserDto> updateUser(User user, long id);
    void deleteById(long id);
    Page<UserDto> findAll(Pageable page);
}
