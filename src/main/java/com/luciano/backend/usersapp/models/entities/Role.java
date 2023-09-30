package com.luciano.backend.usersapp.models.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "roles")
public class Role {

    public Role() {}

    public Role(String name) {
        this.name = name;
    }

    public Role(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String name;

}
