package com.unipi.giguniverse.service;

import com.unipi.giguniverse.dto.ConcertDto;
import com.unipi.giguniverse.dto.VenueDto;
import com.unipi.giguniverse.exceptions.ApplicationException;
import com.unipi.giguniverse.model.*;

import com.unipi.giguniverse.repository.ConcertRepository;
import com.unipi.giguniverse.repository.ReservationRepository;
import com.unipi.giguniverse.repository.TicketRepository;
//import com.unipi.giguniverse.repository.VenueRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class ConcertService {

    /**LoadBalanced Annotation at WebClient.Builder:
     @see com.unipi.giguniverse.GiguniverseApplication
     */
//    private final String VENUE_BASE_URI = "lb://localhost:8081/api/venue";
    private final String VENUE_BASE_URI = "lb://gu-venue-microservice/api/venue";
    //Resilience4j Configuration
    public static final String CONCERT_MICROSERVICE = "ConcertMicroservice";

    private final ConcertRepository concertRepository;
//    private final VenueRepository venueRepository; // Microservices
    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final MailService mailService;
    private final QRGeneratorService qrGeneratorService;
    // Microservices version addition
    private WebClient.Builder webClientBuilder;

    ConcertDto mapConcertToDto(Concert concert){
        Reservation reservation = reservationRepository
                .getOne(Objects.requireNonNull(concert.getReservation()).getReservationId());
        return ConcertDto.builder()
                .concertId(concert.getConcertId())
                .reservationId(concert.getReservation().getReservationId())
                .concertName(concert.getConcertName())
                .description(concert.getDescription())
                .venueId(concert.getVenue().getVenueId())
                .venue(mapVenueToVenueDto(concert.getVenue()))
                .date(concert.getDate())
                .ticketNumber(reservation.getTicketNumber())
                .ticketPrice(reservation.getTicketPrice())
                .image(concert.getImage())
                .build();
    }

    // Monolithic Implementation
//    private Concert mapConcertDto(ConcertDto concertDto){
//        return Concert.builder()
//                .concertName(concertDto.getConcertName())
//                .description(concertDto.getDescription())
//                .venue(venueRepository.getOne(concertDto.getVenueId()))
//                .date(concertDto.getDate())
//                .image(concertDto.getImage())
//                .build();
//    }

    // Microservices Implementation
    @Retry(name = CONCERT_MICROSERVICE)
    @CircuitBreaker(name = CONCERT_MICROSERVICE)
    private Concert mapConcertDto(ConcertDto concertDto){

        Mono<Venue> response = webClientBuilder.build()
                .get()
                .uri(VENUE_BASE_URI +"/" +concertDto.getVenueId())
                .retrieve()
                .bodyToMono(Venue.class);

        return Concert.builder()
                .concertName(concertDto.getConcertName())
                .description(concertDto.getDescription())
                .venue(response.block())
                .date(concertDto.getDate())
                .image(concertDto.getImage())
                .build();
    }

    // Microservices addition from Venue Service
    private VenueDto mapVenueToVenueDto(Venue venue){
        return VenueDto.builder()
                .venueId(venue.getVenueId())
                .venueName(venue.getVenueName())
                .address(venue.getAddress())
                .city(venue.getCity())
                .phone(venue.getPhone())
                .capacity(venue.getCapacity())
                .build();
    }

    public ConcertDto addConcert(ConcertDto concertDto){
        concertRepository.save(mapConcertDto(concertDto));
        return concertDto;
    }

    public ConcertDto getConcertById(Integer id){
        Optional<Concert> concert = concertRepository.findById(id);
        ConcertDto concertDto=mapConcertToDto(concert.orElseThrow(()->new ApplicationException("Concert not found")));
        return concertDto;
    }

    public List<ConcertDto> getAllConcerts(){
        List<ConcertDto> concerts = concertRepository.findAll()
                .stream()
                .map(this::mapConcertToDto)
                .collect(toList());
        return concerts;
    }

    public List<ConcertDto> getConcertByDate(LocalDate date){
        List<ConcertDto> concerts = concertRepository.findByDate(date)
                .stream()
                .map(this::mapConcertToDto)
                .collect(toList());
        return concerts;
    }

    public List<ConcertDto> getConcertByVenue(Venue venue){
        List<ConcertDto> concerts = concertRepository.findByVenue(venue)
                .stream()
                .map(this::mapConcertToDto)
                .collect(toList());
        return concerts;
    }
    public List<ConcertDto> getConcertByMonth(LocalDate date){
        LocalDate start = date.withDayOfMonth(1);
        LocalDate end = date.plusMonths(1).withDayOfMonth(1).minusDays(1);
        List<ConcertDto> concerts = concertRepository.findByDateGreaterThanAndDateLessThan(start,end)
                .stream()
                .map(this::mapConcertToDto)
                .collect(toList());
        return concerts;
    }

    // Monolithic Implementation
//    public List<ConcertDto> getConcertByLoggedInOwner(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        org.springframework.security.core.userdetails.User principal =
//                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
//        Optional<User> owner =  userRepository.findByEmail(principal.getUsername());
//        List<ConcertDto> concerts = concertRepository.findByVenueOwnerUserId(owner.get().getUserId())
//                .stream()
//                .map(this::mapConcertToDto)
//                .collect(toList());
//        return concerts;
//    }

    // Microservices Implementation
    public List<ConcertDto> getConcertByLoggedInOwner(Integer userId){
        List<ConcertDto> concerts = concertRepository.findByVenueOwnerUserId(userId)
                .stream()
                .map(this::mapConcertToDto)
                .collect(toList());
        return concerts;
    }

    // Monolithic Implementation - Dependency on Venue Repository
//    public ConcertDto updateConcert(ConcertDto concertDto){
//        Concert existingConcert = concertRepository.getOne(concertDto.getConcertId());
//        //check for date change
//        boolean changedDate = false;
//        if(!concertDto.getDate().equals(existingConcert.getDate())) changedDate = true;
//        //update concert details
//        Reservation reservation = reservationRepository
//                .getOne(Objects.requireNonNull(existingConcert.getReservation()).getReservationId());
//        existingConcert.setConcertName(concertDto.getConcertName());
//        existingConcert.setDescription(concertDto.getDescription());
//        existingConcert.setVenue(venueRepository.getOne(concertDto.getVenueId()));
//        existingConcert.setDate(concertDto.getDate());
//        existingConcert.setImage(concertDto.getImage());
//        reservation.setTicketPrice(concertDto.getTicketPrice());
//        concertRepository.save(existingConcert);
//        //if concert date has changed then notify users
//        if(changedDate){
//            notifyUserForConcertChanges(existingConcert);
//        }
//        return mapConcertToDto(existingConcert);
//    }

    // Microservices Implementation - Dependency on Venue Endpoint
    @Retry(name = CONCERT_MICROSERVICE)
    @CircuitBreaker(name = CONCERT_MICROSERVICE)
    public ConcertDto updateConcert(ConcertDto concertDto){

        Mono<Venue> response = webClientBuilder.build()
                .get()
                .uri(VENUE_BASE_URI +"/" +concertDto.getVenueId())
                .retrieve()
                .bodyToMono(Venue.class);

        Concert existingConcert = concertRepository.getOne(concertDto.getConcertId());
        //check for date change
        boolean changedDate = false;
        if(!concertDto.getDate().equals(existingConcert.getDate())) changedDate = true;
        //update concert details
        Reservation reservation = reservationRepository
                .getOne(Objects.requireNonNull(existingConcert.getReservation()).getReservationId());
        existingConcert.setConcertName(concertDto.getConcertName());
        existingConcert.setDescription(concertDto.getDescription());
        existingConcert.setVenue(response.block());
        existingConcert.setDate(concertDto.getDate());
        existingConcert.setImage(concertDto.getImage());
        reservation.setTicketPrice(concertDto.getTicketPrice());
        concertRepository.save(existingConcert);
        //if concert date has changed then notify users
        if(changedDate){
            notifyUserForConcertChanges(existingConcert);
        }
        return mapConcertToDto(existingConcert);
    }

    public String deleteConcert(Integer concertId) {
        cancelConcertDeleteTicketsAndUserEmail(concertId);
        Concert existingConcert = concertRepository.getOne(concertId);
        reservationRepository.deleteById(existingConcert.getReservation().getReservationId());
        concertRepository.deleteById(concertId);
        return "Concert with id:" + concertId.toString() + " was deleted.";
    }

    //Assignment1
    public ConcertDto addConcertAndReservation(ConcertDto concertDto){
        //Add Concert
        int concertId = concertRepository.save(mapConcertDto(concertDto)).getConcertId();
        //Get Concert
        Optional<Concert> optConcert = concertRepository.findById(concertId);
        Concert concert = optConcert.orElseThrow(()->new ApplicationException("Concert not found"));
        //Create Reservation
        Reservation reservation = Reservation.builder()
                .concert(concert)
                .owner(concert.getVenue().getOwner())
                .startingDate(Date.from(Instant.now()))
                .finalDate(concert.getDate())
                .ticketNumber(concert.getVenue().getCapacity())
                .ticketPrice(concertDto.getTicketPrice())
                .build();
        //Add Reservation
        int reservationId = reservationRepository.save(reservation).getReservationId();
        concertRepository.getOne(concertId).setReservation(reservation);
        concertDto.setConcertId(concertId);
        concertDto.setReservationId(reservationId);
        concertDto.setTicketNumber(reservation.getTicketNumber());
        concertDto.setVenue(mapVenueToVenueDto(concert.getVenue()));
        return concertDto;
    }

    private void cancelConcertDeleteTicketsAndUserEmail(int concertId){
        Reservation reservation = reservationRepository.findByConcert_ConcertId(concertId)
                .orElseThrow(()->new ApplicationException("Reservation not found"));

        List<Ticket> tickets = ticketRepository.deleteByReservationReservationId(reservation.getReservationId());

        for(Ticket ticket: tickets){
            //Fixes the null connection between classes
            ticket.getReservation().getConcert().getVenue().getVenueName();
            //Send mail to ticket holders
            mailService.cancelConcertNotificationEmail(ticket);
        }
    }

    private void notifyUserForConcertChanges(Concert concert){
        List<Ticket> tickets = ticketRepository
                .findByReservationReservationId(concert.getReservation().getReservationId());
        for(Ticket ticket : tickets){
            String qrCode = qrGeneratorService.generateQRCodeImageToString(ticket);
            mailService.rescheduleConcertNotificationEmail(ticket, qrCode);
        }
    }

}
