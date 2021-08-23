package com.unipi.giguniverse.repository;

import com.unipi.giguniverse.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OwnerRepository  extends JpaRepository<Owner, Integer> {
    Optional<Owner> findByEmail(String email);
}
