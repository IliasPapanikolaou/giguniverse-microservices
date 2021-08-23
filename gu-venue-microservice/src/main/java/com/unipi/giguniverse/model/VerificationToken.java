package com.unipi.giguniverse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.time.Instant;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VerificationToken{

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;
    @OneToOne(fetch = LAZY)
    private User user;
    private String token;
    private Instant expireDate;

}
