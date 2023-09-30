package com.luciano.backend.usersapp.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Entity
@Table(name= "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank       // spring-boot-starter-validation esta anotacion solo es para Strings, para int serian @NotNull
    @Size(min=4, max = 15)
    @Column(name = "username", unique = true)
    private String username;

    @NotBlank
    @Column(name= "password")
    private String password;

    @NotBlank
    @Email
    @Column(name = "email", unique = true)
    private String email;

    @Transient      // con @Transient no se mapea en la base
    private boolean admin;

    @ManyToMany     //un user puede tener muchos roles, un rol puede estar en muchos usuarios
    @JoinTable(name = "users_roles",    //tabla intermedia
            joinColumns = @JoinColumn(name = "user_id"),    //llave foranea del user
            inverseJoinColumns = @JoinColumn(name = "role_id"), //llave foranea del role
            uniqueConstraints = { @UniqueConstraint(columnNames = {"user_id", "role_id"})}) //esta sera la llave primaria que combina las dos foranes
    private List<Role> roles;
}
