package com.unipi.giguniverse.service;

import com.unipi.giguniverse.dto.ReservationDto;
import com.unipi.giguniverse.model.Owner;
import com.unipi.giguniverse.model.Reservation;
import com.unipi.giguniverse.model.User;
import com.unipi.giguniverse.repository.ConcertRepository;
import com.unipi.giguniverse.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ConcertRepository concertRepository;
    private final ConcertService concertService;
//    private final AuthService authService;
//    private final UserRepository userRepository;

    private ReservationDto mapReservationToDto(Reservation reservation){
        return ReservationDto.builder()
                .reservationId(reservation.getReservationId())
                .ticketPrice(Double.valueOf(reservation.getTicketPrice()).intValue())
                .startingDate(reservation.getStartingDate())
                .finalDate(reservation.getFinalDate())
                .ticketNumber(reservation.getTicketNumber())
                .concertId(reservation.getConcert().getConcertId())
                .concert(concertService.mapConcertToDto(reservation.getConcert()))
                .build();
    }

    private Reservation mapReservationDto(ReservationDto reservationDto){
        return Reservation.builder()
                .startingDate(reservationDto.getStartingDate())
                .finalDate(reservationDto.getFinalDate())
                .ticketNumber(reservationDto.getTicketNumber())
                //.owner((Owner) authService.getCurrentUserDetails())
                .owner(reservationDto.getOwner())
                .concert(concertRepository.getOne(reservationDto.getConcertId()))
                .build();
    }

    public ReservationDto getReservationById(Integer id){
        return mapReservationToDto(reservationRepository.getOne(id));
    }

    public ReservationDto addReservation(ReservationDto reservationDto){
        reservationRepository.save(mapReservationDto(reservationDto));
        return reservationDto;
    }

    // Monolithic Implementation
//    public List<ReservationDto> getReservationsByLoggedInOwner(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        org.springframework.security.core.userdetails.User principal =
//                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
//        Optional<User> owner =  userRepository.findByEmail(principal.getUsername());
//        List<ReservationDto> reservations = reservationRepository.findByOwnerUserId(owner.get().getUserId())
//                .stream()
//                .map(this::mapReservationToDto)
//                .collect(toList());
//        return reservations;
//    }

    // Microservices Implementation
    public List<ReservationDto> getReservationsByLoggedInOwner(Integer userId){
        List<ReservationDto> reservations = reservationRepository.findByOwnerUserId(userId)
                .stream()
                .map(this::mapReservationToDto)
                .collect(toList());
        return reservations;
    }

    public ReservationDto updateReservation(ReservationDto reservationDto){
        Reservation existingReservation = reservationRepository.getOne(reservationDto.getReservationId());
        existingReservation.setStartingDate(reservationDto.getStartingDate());
        existingReservation.setFinalDate(reservationDto.getFinalDate());
        existingReservation.setTicketNumber(reservationDto.getTicketNumber());
        return mapReservationToDto(existingReservation);
    }

    public String deleteReservation(Integer reservationId){
        reservationRepository.deleteById(reservationId);
        return "Reservation with id:"+ reservationId.toString() + " was deleted.";
    }
}
