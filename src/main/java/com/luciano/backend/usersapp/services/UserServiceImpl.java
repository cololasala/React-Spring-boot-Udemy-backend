package com.luciano.backend.usersapp.services;

import com.luciano.backend.usersapp.dto.UserDto;
import com.luciano.backend.usersapp.dto.mapper.DtoMapperUser;
import com.luciano.backend.usersapp.models.entities.Role;
import com.luciano.backend.usersapp.models.entities.User;
import com.luciano.backend.usersapp.repositories.RoleRepository;
import com.luciano.backend.usersapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService { // cuando retornamos cosas de repository el tipo es User (seria el Dao), pero al front le daremos un tipo UserDto, por eso debe verificarse bien cuando es un tipo u otro

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository; // para este repository no implementamos un interface solo lo llamamos tal cual

    @Autowired
    private PasswordEncoder passwordEncoder; // al tener el PasswordEncoder declarado como un Bean en SpringSecurityConfig podemos inyectarlo aca

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {    //lo que hacemos es primero traer los users(de tipo User) del repository, luego convertimos cada User de la lista a UserDto para mandarlo al front
        List<User> users = userRepository.findAll();
        return users.stream().map(u -> DtoMapperUser.builder().setUser(u).build()).collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> findAll(Pageable page) { // este findAll utiliza la paginacion
        Page<User> usersPage = userRepository.findAll(page);
        return usersPage.map(u -> DtoMapperUser.builder().setUser(u).build());
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findById(long id) {
      Optional<User> userOptional = userRepository.findById(id);
        if(userOptional.isPresent()) {
            return Optional.of(DtoMapperUser.builder().setUser(userOptional.orElseThrow()).build());    // si esta present retornamos un nuevo optional pero tipo Dto
        }
        return Optional.empty();

        //otra manera de hacer lo de arriba usando map, si es que no existe el user entonces retorna 404 en postman
        //return userRepository.findById(id).map(u -> DtoMapperUser.builder().setUser(u).build());
    }

    @Override
    @Transactional
    public UserDto saveUser(User user) {
        user.setRoles(getRoles(user));       // se setea los roles al usuario
        user.setPassword(passwordEncoder.encode(user.getPassword())); //encriptamos la password
        return DtoMapperUser.builder().setUser(userRepository.save(user)).build();
    }

    @Override
    @Transactional
    public Optional<UserDto> updateUser(User user, long id) {
        Optional<User> userSearch = userRepository.findById(id);
        User userOptional = null;
        if(userSearch.isPresent()) {
            User userToModify = userSearch.orElseThrow(); // devuelve el objeto o lanza exepcion
            userToModify.setRoles(getRoles(user));
            userToModify.setUsername(user.getUsername());
            userToModify.setEmail(user.getEmail());
            userOptional = userRepository.save(userToModify);
        }

        return Optional.ofNullable(DtoMapperUser.builder().setUser(userOptional).build());
      //  return Optional.ofNullable(DtoMapperUser.builder().setUser(userRepository.save(user)).build()); // si no es null devuelve el valor presente, si es null retorna un optional empty
    }

    private List<Role> getRoles(User user) { //retorna una lista de roles para de un user
        List<Role> rolesList = new ArrayList<>();
        Optional<Role> roleUser = roleRepository.findByName("ROLE_USER"); // buscamos el rol
        if(roleUser.isPresent()) {  // si lo encuentra lo añade a la lista de roles
            rolesList.add(roleUser.orElseThrow());
        }

        if(user.isAdmin()) { //si desde front esta marcado com admin
            Optional<Role> roleAdmin = roleRepository.findByName("ROLE_ADMIN");
            if(roleAdmin.isPresent()) {
                rolesList.add(roleAdmin.orElseThrow());
            }
        }
        return rolesList;
    }
}
