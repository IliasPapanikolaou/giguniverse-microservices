package com.unipi.giguniverse.repository;

import antlr.Token;
import com.unipi.giguniverse.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    void deleteByToken(String token);
}
