package com.unipi.giguniverse.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Data
@SuperBuilder ///Builder that supports super from parent class
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true) //Builder that supports super from parent class
public class Owner extends User{

    private String companyName;
    private String vat;
    @OneToMany(fetch = LAZY)
    private List<Venue> venue;
    @OneToMany(fetch = LAZY)
    private List<Reservation> reservation;
}
