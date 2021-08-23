package com.unipi.giguniverse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleLoginRequest {

    @NotBlank(message="id_token is required")
    private String id_token;
    @NotBlank(message="access_token is required")
    private String access_token;
}
