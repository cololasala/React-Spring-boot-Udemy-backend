package com.luciano.backend.usersapp.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class SimpleGrantedAuthorityJsonCreator { //esta clase solo se usa en JwtValidationFilter

    @JsonCreator
    public SimpleGrantedAuthorityJsonCreator(@JsonProperty("authority") String role) { // decimos que pase el rol de la propiedad del json authority, es decir, pasara "ROLE_USER" o "ROLE_ADMIN"

    }
}
