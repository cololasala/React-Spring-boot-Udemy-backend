package com.luciano.backend.usersapp.dto.mapper;

import com.luciano.backend.usersapp.dto.UserDto;
import com.luciano.backend.usersapp.models.entities.User;

public class DtoMapperUser {        //esto sirve para hacer el mapeo entre los datos del user y nuestro dto para mandar al front
                                    //aca se usa el patron Builder
    private User user;
    private DtoMapperUser() {}

    public static DtoMapperUser builder() {
        return new DtoMapperUser();
    }

    public DtoMapperUser setUser(User user) {
        this.user = user;
        return this;      //retorna la instancia DtoMapperUser, esto permite hacer invocar metodos encadenados, la idea es hacer esto -> DtoMapperUser.builder().setUser(userOptional.orElseThrow()).build()
    }

    public UserDto build() {
        if(user == null) {
            throw new RuntimeException("Debe pasar un usuario");
        }
        boolean admin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName())); //busco si el user tiene role admin en su lista de roles
        return new UserDto(this.user.getId(), this.user.getUsername(), this.user.getEmail(), admin);
    }
}
