package com.unipi.giguniverse.security;

import com.unipi.giguniverse.exceptions.ApplicationException;
import com.unipi.giguniverse.exceptions.ApplicationExceptionResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.Date;
import java.time.Instant;

import static io.jsonwebtoken.Jwts.parser;
import static java.util.Date.from;

@Service
public class JwtProvider {

    private KeyStore keyStore;
    @Value("${jwt.expiration.time}")
    private Long jwtExpirationInMillis;

    @PostConstruct
    public void init() {
        try {
            keyStore = KeyStore.getInstance("JKS");
            InputStream resourceAsStream = getClass().getResourceAsStream("/giguniverse.jks");
            keyStore.load(resourceAsStream, "giguniverse".toCharArray());
        }
        catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new ApplicationException("Exception occurred while loading keystore", e);
        }

    }

    public String generateToken(Authentication authentication) {
        org.springframework.security.core.userdetails.User principal = (User) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .setIssuedAt(from(Instant.now()))
                .signWith(getPrivateKey())
                .setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationInMillis)))
                .compact();
    }

    public String generateTokenForGoogleSignIn(Authentication authentication) {
        com.unipi.giguniverse.model.User principal = (com.unipi.giguniverse.model.User) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(principal.getEmail())
                .setIssuedAt(from(Instant.now()))
                .signWith(getPrivateKey())
                .setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationInMillis)))
                .compact();
    }

//    public String generateTokenWithUserName(String username) {
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(from(Instant.now()))
//                .signWith(getPrivateKey())
//                .setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationInMillis)))
//                .compact();
//    }

    private PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) keyStore.getKey("giguniverse", "giguniverse".toCharArray());
        }
        catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new ApplicationException("Exception occurred while retrieving public key from keystore", e);
        }
    }

    public boolean validateToken(String jwt) {
        try{
            parser().setSigningKey(getPublicKey()).parseClaimsJws(jwt);
            return true;
        }
        catch (ExpiredJwtException e){
            throw new ApplicationException("Authentication Failed");
        }
    }

    private PublicKey getPublicKey() {
        try {
            return keyStore.getCertificate("giguniverse").getPublicKey();
        }
        catch (KeyStoreException e) {
            throw new ApplicationException("Exception occurred while retrieving public key from keystore", e);
        }
    }

    public String getUsernameFromJwt(String token) {
        Claims claims = parser()
                .setSigningKey(getPublicKey())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public Long getJwtExpirationInMillis() {
        return jwtExpirationInMillis;
    }
}
