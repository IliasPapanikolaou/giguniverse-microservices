package com.unipi.giguniverse.dto;

import com.unipi.giguniverse.model.Owner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDto {

    private Owner owner;
    private Integer reservationId;
    private Integer concertId;
    private ConcertDto concert;
    private Date startingDate;
    private Date finalDate;
    private Integer ticketPrice;
    private Integer ticketNumber;
}
