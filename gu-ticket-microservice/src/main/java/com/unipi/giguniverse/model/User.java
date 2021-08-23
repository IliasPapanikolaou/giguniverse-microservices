package com.unipi.giguniverse.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.Instant;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Inheritance //Default value Single Table that includes ancestors
@Data //Lombok, Generates Setters - Getters
@SuperBuilder //Builder that supports super
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    protected int userId;
    @NotBlank(message="First Name is required")
    protected String firstname;
    @NotBlank(message="Last Name is required")
    protected String lastname;
    @NotBlank(message="Email is required")
    @Email
    protected String email;
    @NotBlank(message="Password is required")
    protected String password;
    protected Instant created;
    protected Boolean isEnabled = false;
    @Lob
    protected String image;
}

