package com.unipi.giguniverse.repository;

import com.unipi.giguniverse.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TicketRepository extends JpaRepository<Ticket, String> {

    List<Ticket> findByTicketHolderEmail(String email);

    List<Ticket> findByReservationConcertConcertId(Integer concertId);

    List<Ticket> findByTicketBuyerUserId(Integer concertId);

    List<Ticket> findByReservationReservationId(Integer reservationId);

    List<Ticket> deleteByReservationReservationId(Integer reservationId);

//    List<Ticket> findByAttendant(Attendant ticketBuyer);
}
