package com.unipi.giguniverse.repository;

import com.unipi.giguniverse.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Integer> {

    boolean existsVenueByVenueName(String venueName);

    List<Venue> findAllByCity(String city);

    List<Venue> findByOwnerUserId(int userId);
}
