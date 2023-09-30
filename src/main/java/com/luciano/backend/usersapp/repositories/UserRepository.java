package com.luciano.backend.usersapp.repositories;

import com.luciano.backend.usersapp.models.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // se debe usar la palbra clave bien armada (find en este caso es la palabra clave)

    Page<User> findAll(Pageable page); //paginacion

  //  @Query("select u from User u where u.username=?1")  //otra manera de hacer lo mismo, podemos poner el nombre del metodo que querramos, pero debemos crear la consulta
  //  Optional<User> getUserByUsername(String username);
}
