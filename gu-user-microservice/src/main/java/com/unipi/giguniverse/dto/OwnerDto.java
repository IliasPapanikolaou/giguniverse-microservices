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
public class OwnerDto {

    private Integer userId;
    private String firstname;
    private String lastname;
    private String email;
    private String companyName;
    private String vat;
    private Instant created;
    private String image;

}
