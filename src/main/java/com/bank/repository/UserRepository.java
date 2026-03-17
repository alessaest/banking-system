package com.bank.repository;

import com.bank.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    //if username exists, this method checks it
    public boolean usernameExists(String username) {
        return count("username", username) > 0;
    }

    public boolean emailExists(String email) {
        return count("email", email) > 0;
    }

    //for login; ichecheck based sa username and then password
    public Optional<User> authenticate(String username, String password) {
        return find("username = ?1 and password = ?2", username, password).firstResultOptional();
    }


}
