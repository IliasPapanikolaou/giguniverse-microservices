package com.unipi.giguniverse.repository;

import com.unipi.giguniverse.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation,Integer> {

    List<Reservation> findByOwnerUserId(int userId);

    Optional<Reservation> findByConcert_ConcertId (int concertId);
}
