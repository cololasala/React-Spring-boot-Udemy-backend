package com.luciano.backend.usersapp.dto;

import lombok.Data;

@Data
public class UserDto { //dto sirve para especificar la data que quiero mandar al front, en este caso no retornamos la password al front
    private long id;
    private String username;
    private String email;
    private boolean admin;

    UserDto() {}

    public UserDto(long id, String username, String email, boolean admin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.admin = admin;
    }
}
