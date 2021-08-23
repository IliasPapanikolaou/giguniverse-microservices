package com.unipi.giguniverse.repository;

import com.unipi.giguniverse.model.Concert;
import com.unipi.giguniverse.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface ConcertRepository extends JpaRepository<Concert, Integer> {

    List<Concert> findByDate(LocalDate date);
    List<Concert> findByVenue(Venue venue);
    List<Concert> findByDateGreaterThanAndDateLessThan(LocalDate start, LocalDate end);

    List<Concert>findByVenueOwnerUserId(int userId);
}
