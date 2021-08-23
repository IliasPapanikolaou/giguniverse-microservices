package com.unipi.giguniverse.dto;

import com.unipi.giguniverse.model.Owner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VenueDto {

    private Owner owner;
    private Integer venueId;
    private String venueName;
    private String address;
    private String city;
    private String phone;
    private int capacity;

}
