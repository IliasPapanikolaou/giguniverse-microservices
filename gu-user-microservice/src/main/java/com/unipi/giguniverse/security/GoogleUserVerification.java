package com.unipi.giguniverse.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.unipi.giguniverse.exceptions.ApplicationException;
import com.unipi.giguniverse.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@NoArgsConstructor
@Component
public class GoogleUserVerification{

    @Value("${google.ClientId}")
    private String googleClientId;
    //private User user;

    public Boolean verifyGoogleIdToken(String idTokenString){

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList(googleClientId))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null){
                Payload payload = idToken.getPayload();

//                // Print user identifier
//                String userId = payload.getSubject();
//                // Get profile information from payload
//                String email = payload.getEmail();
//                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
//                String name = (String) payload.get("name");
//                String pictureUrl = (String) payload.get("picture");
//                String locale = (String) payload.get("locale");
//                String familyName = (String) payload.get("family_name");
//                String givenName = (String) payload.get("given_name");

                User user = User.builder()
                        .email(payload.getEmail())
                        .firstname(payload.get("given_name").toString())
                        .lastname(payload.get("family_name").toString())
                        .build();

                Authentication authentication = new UsernamePasswordAuthenticationToken(user,
                        null, AuthorityUtils.createAuthorityList("ROLE_ATTENDANT"));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
            else {
                System.out.println("Invalid ID token."); //TODO:Remove sout
                throw new ApplicationException("Invalid ID token");
            }

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            throw new ApplicationException("Invalid ID token");
        }
        return true;
    }

}
