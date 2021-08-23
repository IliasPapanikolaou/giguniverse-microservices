package com.unipi.giguniverse.service;

import com.unipi.giguniverse.dto.ReservationDto;
//import com.unipi.giguniverse.exceptions.ApplicationException;
//import com.unipi.giguniverse.model.Owner;
//import com.unipi.giguniverse.model.Reservation;
//import com.unipi.giguniverse.model.Concert;
import com.unipi.giguniverse.model.User;
//import com.unipi.giguniverse.repository.ConcertRepository;
//import com.unipi.giguniverse.repository.ReservationRepository;
import com.unipi.giguniverse.repository.UserRepository;
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
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class ReservationService {

    private final String RESERVATION_BASE_URI = "lb://localhost:8082/api/reservation";

//    private final ReservationRepository reservationRepository;
//    private final ConcertRepository concertRepository;
//    private final ConcertService concertService;
//    private final AuthService authService;
    private final UserRepository userRepository;
    // Microservices version addition
    private WebClient.Builder webClientBuilder;

//    private ReservationDto mapReservationToDto(Reservation reservation){
//        return ReservationDto.builder()
//                .reservationId(reservation.getReservationId())
//                .startingDate(reservation.getStartingDate())
//                .finalDate(reservation.getFinalDate())
//                .ticketNumber(reservation.getTicketNumber())
//                .concertId(reservation.getConcert().getConcertId())
//                .concert(concertService.mapConcertToDto(reservation.getConcert()))
//                .build();
//    }
//
//    private Reservation mapReservationDto(ReservationDto reservationDto){
//        return Reservation.builder()
//                .startingDate(reservationDto.getStartingDate())
//                .finalDate(reservationDto.getFinalDate())
//                .ticketNumber(reservationDto.getTicketNumber())
//                .owner((Owner) authService.getCurrentUserDetails())
//                .concert(concertRepository.getOne(reservationDto.getConcertId()))
//                .build();
//    }

    // Monolithic Implementation
//    public ReservationDto addReservation(ReservationDto reservationDto){
//        reservationRepository.save(mapReservationDto(reservationDto));
//        return reservationDto;
//    }

    // Microservices Implementation
    @RateLimiter(name = ConcertService.CONCERT_MICROSERVICE)
    @Retry(name = ConcertService.CONCERT_MICROSERVICE)
    @CircuitBreaker(name = ConcertService.CONCERT_MICROSERVICE)
    public ReservationDto addReservation(ReservationDto reservationDto){

        Mono<ReservationDto> response = webClientBuilder.build()
                .post()
                .uri(RESERVATION_BASE_URI)
                .body(Mono.just(reservationDto), ReservationDto.class)
                .retrieve()
                .bodyToMono(ReservationDto.class);

        return response.block();
    }

    // Microservices Implementation
    @RateLimiter(name = ConcertService.CONCERT_MICROSERVICE)
    @Retry(name = ConcertService.CONCERT_MICROSERVICE)
    @CircuitBreaker(name = ConcertService.CONCERT_MICROSERVICE)
    public ReservationDto getReservationById(Integer id){

        Mono<ReservationDto> response = webClientBuilder.build()
                .get()
                .uri(RESERVATION_BASE_URI + "/" +id)
                .retrieve()
                .bodyToMono(ReservationDto.class);

        return response.block();
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
    @RateLimiter(name = ConcertService.CONCERT_MICROSERVICE)
    @Retry(name = ConcertService.CONCERT_MICROSERVICE)
    @CircuitBreaker(name = ConcertService.CONCERT_MICROSERVICE)
    public List<ReservationDto> getReservationsByLoggedInOwner(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        Optional<User> owner =  userRepository.findByEmail(principal.getUsername());

        Mono<List<ReservationDto>> response = webClientBuilder.build()
                .get()
                .uri(RESERVATION_BASE_URI +"/owner/" +owner.get().getUserId())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ReservationDto>>() {
                });

        return response.block();
    }

    // Monolithic Implementation
//    public ReservationDto updateReservation(ReservationDto reservationDto){
//        Reservation existingReservation = reservationRepository.getOne(reservationDto.getReservationId());
//        existingReservation.setStartingDate(reservationDto.getStartingDate());
//        existingReservation.setFinalDate(reservationDto.getFinalDate());
//        existingReservation.setTicketNumber(reservationDto.getTicketNumber());
//        return mapReservationToDto(existingReservation);
//    }

    // Microservices Implementation
    @RateLimiter(name = ConcertService.CONCERT_MICROSERVICE)
    @Retry(name = ConcertService.CONCERT_MICROSERVICE)
    @CircuitBreaker(name = ConcertService.CONCERT_MICROSERVICE)
    public ReservationDto updateReservation(ReservationDto reservationDto){
        Mono<ReservationDto> response = webClientBuilder.build()
                .put()
                .uri(RESERVATION_BASE_URI +"/update")
                .body(Mono.just(reservationDto), ReservationDto.class)
                .retrieve()
                .bodyToMono(ReservationDto.class);

        return response.block();
    }

    // Monolithic Implementation
//    public String deleteReservation(Integer reservationId){
//        reservationRepository.deleteById(reservationId);
//        return "Reservation with id:"+ reservationId.toString() + " was deleted.";
//    }

    // Microservices Implementation
    @RateLimiter(name = ConcertService.CONCERT_MICROSERVICE)
    @Retry(name = ConcertService.CONCERT_MICROSERVICE)
    @CircuitBreaker(name = ConcertService.CONCERT_MICROSERVICE)
    public String deleteReservation(Integer reservationId){
        Mono<String> response = webClientBuilder.build()
                .delete()
                .uri(RESERVATION_BASE_URI +"/delete/" +reservationId)
                .retrieve()
                .bodyToMono(String.class);
        return response.block();
    }
}
