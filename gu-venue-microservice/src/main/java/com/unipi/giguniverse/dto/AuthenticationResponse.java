package com.unipi.giguniverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    private String firstname;
    private String lastname;
    private String email;
    private String role;
    private int id;
    private String jwt;
    private Instant expiresAt;
//    private String refreshToken;

}
