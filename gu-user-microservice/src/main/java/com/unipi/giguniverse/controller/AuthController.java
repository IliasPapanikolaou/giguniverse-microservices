package com.unipi.giguniverse.controller;

import com.unipi.giguniverse.dto.*;
import com.unipi.giguniverse.service.AuthService;
import com.unipi.giguniverse.service.RefreshTokenService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/signup-owner")
    public ResponseEntity<String> signupOwner(@Valid @RequestBody RegisterOwnerRequest registerOwnerRequest){
        authService.ownerSignup(registerOwnerRequest);
        return new ResponseEntity<>("Owner Registration Successful", HttpStatus.OK);
    }

    @PostMapping("/signup-attendant")
    public ResponseEntity<String> signupAttendant(@Valid @RequestBody RegisterAttendantRequest registerAttendantRequest){
        authService.attendantSignup(registerAttendantRequest);
        return new ResponseEntity<>("Attendant Registration Successful", HttpStatus.OK);
    }

    @GetMapping("account-verification/{token}")
    public ResponseEntity<String> verifyAccount(@PathVariable String token){
        authService.verifyAccount(token);
        return new ResponseEntity<>("Account activated", HttpStatus.OK);
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }

    @PostMapping("/googleLogin")
    public AuthenticationResponse googleLogin(@Valid @RequestBody GoogleLoginRequest googleLoginRequest){
        return authService.googleLogin(googleLoginRequest);
    }

//    @PostMapping("refresh/token")
//    public AuthenticationResponse refreshTokens(@RequestBody RefreshTokenRequest refreshTokenRequest){
//        return authService.refreshToken(refreshTokenRequest);
//    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequest refreshTokenRequest){
        refreshTokenService.deleteRefreshToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK).body("Refresh Token Deleted Successfully");
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error)->{
            String fieldName = ((FieldError)error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }


}
