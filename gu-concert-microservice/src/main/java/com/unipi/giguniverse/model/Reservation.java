package com.unipi.giguniverse.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.Date;
import java.util.List;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private int reservationId;
    @OneToOne(fetch = LAZY)
    private Concert concert;
    @OneToOne(fetch = LAZY)
    private Owner owner;
    private Date startingDate;
    private Date finalDate;
    private double ticketPrice;
    private int ticketNumber;
    @OneToMany(fetch = LAZY)
    private List<Ticket> tickets;
}
