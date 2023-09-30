package com.luciano.backend.usersapp.auth.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luciano.backend.usersapp.models.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.*;

import static com.luciano.backend.usersapp.auth.JwtTokenConfig.*;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter { //este filtro sera llamado cuando la ruta sea login y por cada metodo POST
    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override       //metodo para intentar hacer login/autenticacion (es lo primero que se ejecuta cuando la ruta es /login)
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException { // el request obtiene el body
        User user = null;
        String username = null;
        String password = null;

        try {
            user = new ObjectMapper().readValue(request.getInputStream(), User.class); // para poblar la instancia leemos los valores del request y lo asociamos a la clase User
            username = user.getUsername();
            password = user.getPassword();
            logger.info("Username desde request " + username);
            logger.info("Password desde request " + password);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(authToken);
    }

    @Override //metodo login exitoso
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String username = ((UserDetails)authResult.getPrincipal()).getUsername(); //UserDetails de spring security

        //esto comentado es lo que usabamos antes de usar JWT
        //  String originalInput = SECRET_KEY + "." + username; // el token sera distinto por cada usuario ya que usamos el usernam
        //  String token = Base64.getEncoder().encodeToString(originalInput.getBytes());  //encriptamos

        //obtenemos los roles desde authResult y lo seteamos en el claims del token
        Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();
        boolean admin = roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN")); // si algun role es admin lo seteamos como true en el claims
        Claims claims = Jwts.claims();
        claims.put("authorities", new ObjectMapper().writeValueAsString(roles));
        claims.put("admin", new ObjectMapper().writeValueAsString(admin));
        claims.put("username", username);

        //creamos el token usando como subject el username, el claims y que firmara con la SECRET_KEY, le seteamos fecha de creacion y expiracion(1 hora)
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username).signWith(SECRET_KEY)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + 3600000))
                .compact();

        response.addHeader(HEADER_AUTHORIZATION, PREFIX_TOKEN + token);     //envio al response el token en header

        Map<String, String> body = new HashMap<>();
        body.put("token", token);
        body.put("message", String.format("Usuario '%s' ha iniciado sesion con token", username));
        body.put("username", username);
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));    //envio body como json al cuerpo de la response
        response.setStatus(200);
        response.setContentType("application/json");
    }

    @Override //metodo login sin exito
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Error en la autenticacion username o password es incorrecto");
        body.put("error", failed.getMessage());
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setStatus(401);        //error 401 no autorizado
        response.setContentType("application/json");
    }
}
