package com.unipi.giguniverse.repository;

import com.unipi.giguniverse.model.Owner;
import com.unipi.giguniverse.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Boolean existsUserByEmail(String email);
}
