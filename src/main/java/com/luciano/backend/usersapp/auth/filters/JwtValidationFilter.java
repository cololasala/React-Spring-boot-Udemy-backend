package com.luciano.backend.usersapp.auth.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luciano.backend.usersapp.auth.SimpleGrantedAuthorityJsonCreator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.luciano.backend.usersapp.auth.JwtTokenConfig.*;

public class JwtValidationFilter extends BasicAuthenticationFilter {    //este filtro sirve para la validacion del token

    public JwtValidationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader(HEADER_AUTHORIZATION);

        if(header == null || !header.startsWith(PREFIX_TOKEN)) {
            chain.doFilter(request, response);  //continuamos con la cadena de filtros y salimos (es decir, no validamos nada y salimos)
            return;
        }

        String token = header.replace(PREFIX_TOKEN, ""); //sacamos la cadena Bearer y nos quedamos con el token

        //esto comentado es lo que usabamos antes de usar JWT
     //   byte[] tokenBytes = Base64.getDecoder().decode(token); //el decode retorna tipo byte[]
     //   String tokenDecoded = new String(tokenBytes);
     //   String[] tokenArr = tokenDecoded.split("\\."); //como usamos separador el punto, debemos hacer el split asi ya que el punto solo es carater reservado
    //    String secret = tokenArr[0];
    //    String username = tokenArr[1];

        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody(); // realizamos la validacion del token con la secret key y si sale todo bien guardamos las claims
            String username = claims.getSubject(); //obtenemos el username
            Object authoritiesClaims = claims.get("authorities"); //obtenemos el authorities
            Collection<? extends GrantedAuthority> authorities = Arrays
                    .asList(new ObjectMapper()
                            .addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityJsonCreator.class) // con esto modificamos el constructor de la clase original SimpleGrantedAuthority, para usar el constructor de nuestra clase abstracta creada
                            .readValue(authoritiesClaims.toString().getBytes(), SimpleGrantedAuthority[].class)); //convertimos authoritiesClaims a un array de SimpleGrantedAuthority

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } catch (JwtException e) {
            Map<String, String> body = new HashMap<>();
            body.put("error", e.getMessage());
            body.put("message", "Token invalido");
            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            response.setStatus(401);
            response.setContentType("application/json");
        }
    }
}
