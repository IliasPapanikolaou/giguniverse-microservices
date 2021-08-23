package com.unipi.giguniverse.service;

import com.unipi.giguniverse.dto.ConcertDto;
import com.unipi.giguniverse.dto.VenueDto;
import com.unipi.giguniverse.exceptions.ApplicationException;
import com.unipi.giguniverse.model.*;

import com.unipi.giguniverse.repository.*;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class ConcertService {
    /**LoadBalanced Annotation at WebClient.Builder:
     @see com.unipi.giguniverse.GiguniverseApplication
     */
//    private final String CONCERT_BASE_URI = "lb://localhost:8082/api/concert";
    private final String CONCERT_BASE_URI = "lb://gu-concert-microservice/api/concert";
//    private final String RESERVATION_BASE_URI = "lb://localhost:8082/api/reservation";
    private final String RESERVATION_BASE_URI = "lb://gu-concert-microservice/api/reservation";
    //Resilience4j Configuration
    public static final String CONCERT_MICROSERVICE = "ConcertMicroservice";

//    private final ConcertRepository concertRepository;
//    private final VenueRepository venueRepository;
    private final VenueService venueService;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
//    private final TicketRepository ticketRepository;
    private final MailService mailService;
    private final QRGeneratorService qrGeneratorService;
    // Microservices version addition
    private WebClient.Builder webClientBuilder;

    // Monolithic Implementation
//    ConcertDto mapConcertToDto(Concert concert){
//        Reservation reservation = reservationRepository
//                .getOne(Objects.requireNonNull(concert.getReservation()).getReservationId());
//        return ConcertDto.builder()
//                .concertId(concert.getConcertId())
//                .concertName(concert.getConcertName())
//                .description(concert.getDescription())
//                .venueId(concert.getVenue().getVenueId())
//                .venue(venueService.mapVenueToVenueDto(concert.getVenue()))
//                .date(concert.getDate())
//                .ticketNumber(reservation.getTicketNumber())
//                .ticketPrice(reservation.getTicketPrice())
//                .image(concert.getImage())
//                .build();
//    }

    // Microservices Implementation
    @Retry(name = CONCERT_MICROSERVICE)
    @CircuitBreaker(name = CONCERT_MICROSERVICE)
    ConcertDto mapConcertToDto(Concert concert){

        Mono<Reservation> response = webClientBuilder.build()
                .get()
                .uri(RESERVATION_BASE_URI +"/" + concert.getReservation().getReservationId())
                .retrieve()
                .bodyToMono(Reservation.class);

        Reservation reservation = response.block();

        return ConcertDto.builder()
                .concertId(concert.getConcertId())
                .concertName(concert.getConcertName())
                .description(concert.getDescription())
                .venueId(concert.getVenue().getVenueId())
                .venue(venueService.mapVenueToVenueDto(concert.getVenue()))
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
    private Concert mapConcertDto(ConcertDto concertDto){

        return Concert.builder()
                .concertName(concertDto.getConcertName())
                .description(concertDto.getDescription())
                .venue(venueService.mapVenueDtoToVenue(venueService.getVenueById(concertDto.getVenueId())))
                .date(concertDto.getDate())
                .image(concertDto.getImage())
                .build();
    }

//    public ConcertDto addConcert(ConcertDto concertDto){
//        concertRepository.save(mapConcertDto(concertDto));
//        return concertDto;
//    }

    // Monolithic Implementation
//    public ConcertDto getConcertById(Integer id){
//        Optional<Concert> concert = concertRepository.findById(id);
//        ConcertDto concertDto=mapConcertToDto(concert.orElseThrow(()->new ApplicationException("Concert not found")));
//        return concertDto;
//    }

    // Microservices Implementation - Reactive programming
    @RateLimiter(name = CONCERT_MICROSERVICE)
    @Retry(name = CONCERT_MICROSERVICE)
    @CircuitBreaker(name = CONCERT_MICROSERVICE)
    public ConcertDto getConcertById(Integer id){
        Mono<ConcertDto> response = webClientBuilder.build()
                .get()
                .uri(CONCERT_BASE_URI +"/" +id)
                .retrieve()
                .bodyToMono(ConcertDto.class);
        return response.block();
    }

    // Monolithic Implementation
//    public List<ConcertDto> getAllConcerts(){
//        List<ConcertDto> concerts = concertRepository.findAll()
//                .stream()
//                .map(this::mapConcertToDto)
//                .collect(toList());
//        return concerts;
//    }

    // Microservices Implementation - Reactive programming
    @RateLimiter(name = CONCERT_MICROSERVICE)
    @Retry(name = CONCERT_MICROSERVICE, fallbackMethod = "retryLog")
    @CircuitBreaker(name = CONCERT_MICROSERVICE, fallbackMethod = "circuitBreakerLog")
    public List<ConcertDto> getAllConcerts(){

        Mono<List<ConcertDto>> response = webClientBuilder.build()
                .get()
                .uri(CONCERT_BASE_URI)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ConcertDto>>() {
                });

        List<ConcertDto> concertDtos = response.block();
        return concertDtos;
    }

    // Monolithic Implementation
//    public List<ConcertDto> getConcertByDate(LocalDate date){
//        List<ConcertDto> concerts = concertRepository.findByDate(date)
//                .stream()
//                .map(this::mapConcertToDto)
//                .collect(toList());
//        return concerts;
//    }

    // Microservices Implementation - Reactive programming
    @RateLimiter(name = CONCERT_MICROSERVICE)
    @Retry(name = CONCERT_MICROSERVICE, fallbackMethod = "retryLog")
    @CircuitBreaker(name = CONCERT_MICROSERVICE, fallbackMethod = "circuitBreakerLog")
    public List<ConcertDto> getConcertByDate(LocalDate date){

        Mono<List<ConcertDto>> response = webClientBuilder.build()
                .get()
                .uri(CONCERT_BASE_URI +"/date/" +date)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ConcertDto>>() {
                });

        List<ConcertDto> concertDtos = response.block();
        return concertDtos;
    }

    // Monolithic Implementation
//    public List<ConcertDto> getConcertByVenue(Venue venue){
//        List<ConcertDto> concerts = concertRepository.findByVenue(venue)
//                .stream()
//                .map(this::mapConcertToDto)
//                .collect(toList());
//        return concerts;
//    }

    // Microservices Implementation - Reactive programming
    @RateLimiter(name = CONCERT_MICROSERVICE)
    @Retry(name = CONCERT_MICROSERVICE, fallbackMethod = "retryLog")
    @CircuitBreaker(name = CONCERT_MICROSERVICE, fallbackMethod = "circuitBreakerLog")
    public List<ConcertDto> getConcertByVenue(Venue venue){
        Mono<List<ConcertDto>> response = webClientBuilder.build()
                .get()
                .uri(CONCERT_BASE_URI +"/venue/" +venue)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ConcertDto>>() {
                });

        List<ConcertDto> concertDtos = response.block();
        return concertDtos;
    }

    // Monolithic Implementation
//    public List<ConcertDto> getConcertByMonth(LocalDate date){
//        LocalDate start = date.withDayOfMonth(1);
//        LocalDate end = date.plusMonths(1).withDayOfMonth(1).minusDays(1);
//        List<ConcertDto> concerts = concertRepository.findByDateGreaterThanAndDateLessThan(start,end)
//                .stream()
//                .map(this::mapConcertToDto)
//                .collect(toList());
//        return concerts;
//    }

    // Microservices Implementation - Reactive programming
    @RateLimiter(name = CONCERT_MICROSERVICE)
    @Retry(name = CONCERT_MICROSERVICE, fallbackMethod = "retryLog")
    @CircuitBreaker(name = CONCERT_MICROSERVICE, fallbackMethod = "circuitBreakerLog")
    public List<ConcertDto> getConcertByMonth(LocalDate date){

        Mono<List<ConcertDto>> response = webClientBuilder.build()
                .get()
                .uri(CONCERT_BASE_URI +"/month/" +date)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ConcertDto>>() {
                });

        List<ConcertDto> concertDtos = response.block();
        return concertDtos;
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

    // Microservices Implementation - Reactive programming
    @RateLimiter(name = CONCERT_MICROSERVICE)
    @Retry(name = CONCERT_MICROSERVICE, fallbackMethod = "retryLog")
    @CircuitBreaker(name = CONCERT_MICROSERVICE, fallbackMethod = "circuitBreakerLog")
    public List<ConcertDto> getConcertByLoggedInOwner(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        Optional<User> owner =  userRepository.findByEmail(principal.getUsername());

        Mono<List<ConcertDto>> response = webClientBuilder.build()
                .get()
                .uri(CONCERT_BASE_URI +"/owner/" +owner.get().getUserId())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ConcertDto>>() {
                });

        List<ConcertDto> concertDtos = response.block();
        return concertDtos;
    }

    // Monolithic Implementation
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

    // Microservices Implementation - Reactive programming
    @RateLimiter(name = CONCERT_MICROSERVICE)
    @Retry(name = CONCERT_MICROSERVICE)
    @CircuitBreaker(name = CONCERT_MICROSERVICE)
    public ConcertDto updateConcert(ConcertDto concertDto){
        Mono<ConcertDto> response = webClientBuilder.build()
                .put()
                .uri(CONCERT_BASE_URI +"/update/")
                .body(Mono.just(concertDto), ConcertDto.class)
                .retrieve()
                .bodyToMono(ConcertDto.class);

        return response.block();
    }

    // Monolithic Implementation
//    public String deleteConcert(Integer concertId) {
//        cancelConcertDeleteTicketsAndUserEmail(concertId);
//        Concert existingConcert = concertRepository.getOne(concertId);
//        reservationRepository.deleteById(existingConcert.getReservation().getReservationId());
//        concertRepository.deleteById(concertId);
//        return "Concert with id:" + concertId.toString() + " was deleted.";
//    }

    // Microservices Implementation - Reactive programming
    @RateLimiter(name = CONCERT_MICROSERVICE)
    @Retry(name = CONCERT_MICROSERVICE)
    @CircuitBreaker(name = CONCERT_MICROSERVICE)
    public String deleteConcert(Integer concertId) {
        Mono<String> response = webClientBuilder.build()
                .delete()
                .uri(CONCERT_BASE_URI +"/delete/" +concertId)
                .retrieve()
                .bodyToMono(String.class);
        return response.block();
    }

    // Monolithic Implementation - Assignment1
//    public ConcertDto addConcertAndReservation(ConcertDto concertDto){
//        //Add Concert
//        int concertId = concertRepository.save(mapConcertDto(concertDto)).getConcertId();
//        //Get Concert
//        Optional<Concert> optConcert = concertRepository.findById(concertId);
//        Concert concert = optConcert.orElseThrow(()->new ApplicationException("Concert not found"));
//        //Create Reservation
//        Reservation reservation = Reservation.builder()
//                .concert(concert)
//                .owner(concert.getVenue().getOwner())
//                .startingDate(Date.from(Instant.now()))
//                .finalDate(concert.getDate())
//                .ticketNumber(concert.getVenue().getCapacity())
//                .ticketPrice(concertDto.getTicketPrice())
//                .build();
//        //Add Reservation
//        reservationRepository.save(reservation);
//        concertRepository.getOne(concertId).setReservation(reservation);
//        concertDto.setConcertId(concertId);
//        concertDto.setTicketNumber(reservation.getTicketNumber());
//        concertDto.setVenue(venueService.mapVenueToVenueDto(concert.getVenue()));
//        return concertDto;
//    }

    // Microservices Implementation - Reactive programming
    @RateLimiter(name = CONCERT_MICROSERVICE)
    @Retry(name = CONCERT_MICROSERVICE)
    @CircuitBreaker(name = CONCERT_MICROSERVICE)
    public ConcertDto addConcertAndReservation(ConcertDto concertDto){
        //Add Concert
        Mono<ConcertDto> response = webClientBuilder.build()
                .post()
                .uri(CONCERT_BASE_URI)
                .body(Mono.just(concertDto), ConcertDto.class)
                .retrieve()
                .bodyToMono(ConcertDto.class);

        return response.block();
    }

    // Changes notifications happen in Concert Microservice
//    private void cancelConcertDeleteTicketsAndUserEmail(int concertId){
//        Reservation reservation = reservationRepository.findByConcert_ConcertId(concertId)
//                .orElseThrow(()->new ApplicationException("Reservation not found"));
//
//        List<Ticket> tickets = ticketRepository.deleteByReservationReservationId(reservation.getReservationId());
//
//        for(Ticket ticket: tickets){
//            //Fixes the null connection between classes
//            ticket.getReservation().getConcert().getVenue().getVenueName();
//            //Send mail to ticket holders
//            mailService.cancelConcertNotificationEmail(ticket);
//        }
//    }

    // Changes notifications happen in Concert Microservice
//    private void notifyUserForConcertChanges(Concert concert){
//        List<Ticket> tickets = ticketRepository
//                .findByReservationReservationId(concert.getReservation().getReservationId());
//        for(Ticket ticket : tickets){
//            String qrCode = qrGeneratorService.generateQRCodeImageToString(ticket);
//            mailService.rescheduleConcertNotificationEmail(ticket, qrCode);
//        }
//    }

    // Catch only CallNotPermittedException Exception, unless will log all exceptions
    public List<ConcertDto> circuitBreakerLog(CallNotPermittedException ex){
        log.warn("CircuitBreaker Open!!!");
        return null;
    }

    public List<ConcertDto> retryLog(Throwable ex){
        log.warn("Retry Triggered!!!");
        return null;
    }
}
