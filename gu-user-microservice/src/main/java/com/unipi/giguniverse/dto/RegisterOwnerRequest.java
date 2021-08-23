package com.unipi.giguniverse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterOwnerRequest {

    @NotBlank(message="First Name is required")
    private String firstname;
    @NotBlank(message="Last Name is required")
    private String lastname;
    @NotBlank(message="Email is required")
    @Email
    private String email;
    @NotBlank(message="Password is required")
    private String password;
    private String companyName;
    private String vat;
}
