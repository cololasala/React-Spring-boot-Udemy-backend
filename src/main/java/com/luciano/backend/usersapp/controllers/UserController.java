package com.luciano.backend.usersapp.controllers;

import com.luciano.backend.usersapp.dto.UserDto;
import com.luciano.backend.usersapp.models.entities.User;
import com.luciano.backend.usersapp.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(originPatterns = "*")
public class UserController {

    @Autowired
    private UserService userService; // usamos el service

    @GetMapping("/users")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{page}")
    public Page<UserDto> getPaginationUsers(@PathVariable int page) {
        Pageable pageable = PageRequest.of(page, 3); // pasamos el nro de pagina y el tamanio por pagina sera 3
        return userService.findAll(pageable);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable long id) {
        Optional<UserDto> userOptional = userService.findById(id);
        if(userOptional.isPresent()) {  // si existe
            return ResponseEntity.ok(userOptional.orElseThrow());   //si falla lanza exepcion
        }
        return ResponseEntity.notFound().build();   // si no lo encuentra
    }

    @PostMapping("/user")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user, BindingResult result) {     //@Valid valida el request body, BindingResult es el objeto resultado de la validacion de @Valid
        if(result.hasErrors()) {
            return validation(result);
        }
        UserDto newUser = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser); //enviamos el body con el status 201
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable long id, @Valid @RequestBody User user, BindingResult result) {
        if(result.hasErrors()) {
            return validation(result);
        }
        Optional<UserDto> optionalUser = userService.updateUser(user, id);
        if(optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(optionalUser.orElseThrow());
        }
        return ResponseEntity.notFound().build(); // retorna un 404
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable long id) {
        Optional<UserDto> userSearch = userService.findById(id);
        if(userSearch.isPresent()) {
            userService.deleteById(id);
            return ResponseEntity.noContent().build(); // retorna un 204
        }
        return ResponseEntity.notFound().build(); // retorna un 404
    }

    private ResponseEntity<?> validation(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "The field " + err.getField() + " " + err.getDefaultMessage());  //lleno map con Key, value
        });
        return ResponseEntity.badRequest().body(errors); //otra manera de hacerlo -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors)
    }
}
