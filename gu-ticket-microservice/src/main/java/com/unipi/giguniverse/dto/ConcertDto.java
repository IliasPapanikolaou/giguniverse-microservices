package com.unipi.giguniverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConcertDto {

    private Integer concertId;
    private Integer reservationId;
    @NotBlank(message = "Concert Name Required")
    private String concertName;
    private String description;
    @NotBlank(message = "Venue ID Required")
    private Integer venueId;
    private VenueDto venue;
    @NotBlank(message = "Date Required")
    private Date date;
    @NotBlank(message = "Concert Price Required")
    private double ticketPrice;
    @NotBlank(message = "Number of Tickets Required")
    private Integer ticketNumber;
    private String image;
}