package com.luciano.backend.usersapp.services;

import com.luciano.backend.usersapp.repositories.RoleRepository;
import com.luciano.backend.usersapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service        //este service se realiza por atras(no se invoca en ningun lado, pero sera usado por el AuthenticationManager en SpringSecurityConfig
public class JpaUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<com.luciano.backend.usersapp.models.entities.User> optionalUser = userRepository.findByUsername(username); //buscamos el user en la base
        if(!optionalUser.isPresent()) {
            throw new UsernameNotFoundException(String.format("Usenarme %s no existe en el sistema", username));
        }
        com.luciano.backend.usersapp.models.entities.User user = optionalUser.orElseThrow();    //obtenemos el user o tira exepcion

        List<GrantedAuthority> authorities = user.getRoles().stream()//por cada role se crea un SimpleGrantedAuthority con el name del role
                .map(r -> new SimpleGrantedAuthority(r.getName()))
                .collect(Collectors.toList());

        return new User(user.getUsername(), //retornamos un user(de CredentialsUser) con su username, password y roles
                user.getPassword(),
                true,
                true,
                true,
                true,
                authorities);
    }
}
