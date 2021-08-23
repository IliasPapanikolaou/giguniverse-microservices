package com.unipi.giguniverse.service;

import com.unipi.giguniverse.dto.*;
import com.unipi.giguniverse.exceptions.ApplicationException;
import com.unipi.giguniverse.model.*;
import com.unipi.giguniverse.repository.UserRepository;
import com.unipi.giguniverse.repository.VerificationTokenRepository;
import com.unipi.giguniverse.security.GoogleUserVerification;
import com.unipi.giguniverse.security.JwtProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    private final JwtProvider jwtProvider;
    //private final RefreshTokenService refreshTokenService;
    private final GoogleUserVerification googleUserVerification;

    public void ownerSignup(RegisterOwnerRequest registerOwnerRequest){

        User owner = Owner.builder()
                .firstname(registerOwnerRequest.getFirstname())
                .lastname(registerOwnerRequest.getLastname())
                .email(registerOwnerRequest.getEmail())
                .password(passwordEncoder.encode(registerOwnerRequest.getPassword()))
                .companyName(registerOwnerRequest.getCompanyName())
                .vat(registerOwnerRequest.getVat())
                .created(Instant.now())
                .isEnabled(false)
                .build();

        //Check DB for duplicate account by email
        if(!userRepository.existsUserByEmail(registerOwnerRequest.getEmail())){

            userRepository.save(owner); // Save User to DB

            String token = generateVerificationToken(owner);
            mailService.sendActivationEMail(new NotificationEmail("Please Activate your Account",
                    owner.getEmail(), "http://localhost:8080/api/auth/account-verification/" +token));
        }
        else {
            throw new ApplicationException("User already exists");
        }
    }

    public void attendantSignup(RegisterAttendantRequest registerAttendantRequest){

        User attendant = Attendant.builder()
                .firstname(registerAttendantRequest.getFirstname())
                .lastname(registerAttendantRequest.getLastname())
                .email(registerAttendantRequest.getEmail())
                .password(passwordEncoder.encode(registerAttendantRequest.getPassword()))
                .created(Instant.now())
                .isEnabled(false)
                .build();

        //Check DB for duplicate account by email
        if(!userRepository.existsUserByEmail(registerAttendantRequest.getEmail())){

            userRepository.save(attendant); // Save User to DB

            String token = generateVerificationToken(attendant);
            mailService.sendActivationEMail(new NotificationEmail("Please Activate your Account",
                    attendant.getEmail(), "http://localhost:8080/api/auth/account-verification/" +token));
        }
        else {
            throw new ApplicationException("User already exists");
        }
    }

    //Create Verification token
    private String generateVerificationToken(User user){
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);

        verificationTokenRepository.save(verificationToken);
        return token;
    }

    //Account Verification
    public void verifyAccount(String token){
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        verificationToken.orElseThrow(()->new ApplicationException("Invalid Token"));
        //Fetch and Enable User
        fetchUserAndEnable(verificationToken.get());
        //Delete Verification Token from DB
        deleteVerificationToken(token);
    }

    //Check if token is valid
    private void fetchUserAndEnable(VerificationToken verificationToken){
        Integer userId = verificationToken.getUser().getUserId();
        User user = userRepository.findById(userId).orElseThrow(()->new ApplicationException("User not found"));
        user.setIsEnabled(true);
        userRepository.save(user);
    }

    //Delete Verification Token
    private void deleteVerificationToken(String token){
        verificationTokenRepository.deleteByToken(token);
    }

    //Login Method
    public AuthenticationResponse login(LoginRequest loginRequest){
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        //Load current User
        User user = getCurrentUserDetails();
        String token = jwtProvider.generateToken(authenticate);
        return AuthenticationResponse.builder()
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .role(user.getClass().getSimpleName())
                .id(user.getUserId())
                .jwt(token)
//                .refreshToken(refreshTokenService.generateRefreshToken().getToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .build();
    }

    //Google Login
    public AuthenticationResponse googleLogin(GoogleLoginRequest googleLoginRequest){

        if(googleUserVerification.verifyGoogleIdToken(googleLoginRequest.getId_token())){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User principal = (User) authentication.getPrincipal();
            if(userRepository.existsUserByEmail(principal.getEmail())){
                Optional<User> userOptional = userRepository.findByEmail(principal.getEmail());
                User googleUser = userOptional.orElseThrow(()->new ApplicationException("User not found"));
                String token = jwtProvider.generateTokenForGoogleSignIn(authentication);
                return AuthenticationResponse.builder()
                        .firstname(googleUser.getFirstname())
                        .lastname(googleUser.getLastname())
                        .email(googleUser.getEmail())
                        .role(googleUser.getClass().getSimpleName())
                        .id(googleUser.getUserId())
                        .jwt(token)
//                .refreshToken(refreshTokenService.generateRefreshToken().getToken())
                        .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                        .build();
            }
            else {
                User attendant = Attendant.builder()
                        .firstname(principal.getFirstname())
                        .lastname(principal.getLastname())
                        .email(principal.getEmail())
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .created(Instant.now())
                        .isEnabled(true)
                        .build();

                userRepository.save(attendant);// Save User to DB
                return googleLogin(googleLoginRequest);
            }
        }
        else return null;
    }

    //Get current logged in user details from DB
    public User getCurrentUserDetails(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        Optional<User> userOptional = userRepository.findByEmail(principal.getUsername());

        return userOptional.orElseThrow(()->new ApplicationException("User not found"));
    }

//    //Checks if user has UUID RefreshToken in DB, if yes, it refreshes the JWToken
//    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
//        refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
//        String token = jwtProvider.generateTokenWithUserName(refreshTokenRequest.getEmail());
//        return AuthenticationResponse.builder()
//                .jwt(token)
//                .refreshToken(refreshTokenRequest.getRefreshToken())
//                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
//                .email(refreshTokenRequest.getEmail())
//                .build();
//    }
}
