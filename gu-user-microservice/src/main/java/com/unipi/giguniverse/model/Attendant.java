package com.unipi.giguniverse.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Data
@SuperBuilder ///Builder that supports super from parent class
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true) //Builder that supports super from parent class
public class Attendant extends User{

    @OneToMany
    private List<Ticket> tickets;
}
