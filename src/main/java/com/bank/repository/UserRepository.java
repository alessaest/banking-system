package com.bank.repository;

import com.bank.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;


//handles the database queries related to users
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    //if username exists, this method checks it
    public boolean usernameExists(String username) {
        return count("username", username) > 0;
    }

    public boolean emailExists(String email) {
        return count("email", email) > 0;
    }
}
