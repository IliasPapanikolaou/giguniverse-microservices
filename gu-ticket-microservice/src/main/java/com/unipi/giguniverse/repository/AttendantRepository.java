package com.unipi.giguniverse.repository;

import com.unipi.giguniverse.model.Attendant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendantRepository extends JpaRepository<Attendant, Integer> {
    Optional<Attendant> findByEmail(String email);
}
