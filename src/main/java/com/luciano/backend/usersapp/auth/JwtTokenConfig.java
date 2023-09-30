package com.luciano.backend.usersapp.auth;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

//solo seran constantes reutilizables
public class JwtTokenConfig {
   // public static final String SECRET_KEY = "algun_token_con_frase_secreta";
    public static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256); // con esto se genera una clave segura aleatoria
    public static final String PREFIX_TOKEN = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";
}
