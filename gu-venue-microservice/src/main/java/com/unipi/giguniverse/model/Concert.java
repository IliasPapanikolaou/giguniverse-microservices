package com.unipi.giguniverse.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Concert {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private int concertId;
    private String concertName;
    @ManyToOne(fetch= LAZY)
    private Venue venue;
    @Nullable
    @Lob
    private String description;
    private Date date;
    @Nullable
    @OneToOne(fetch = LAZY)
    private Reservation reservation;
    @Lob
    private String image;

}
