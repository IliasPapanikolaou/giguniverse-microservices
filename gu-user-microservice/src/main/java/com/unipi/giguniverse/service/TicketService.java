package com.unipi.giguniverse.service;

import com.unipi.giguniverse.dto.*;
//import com.unipi.giguniverse.exceptions.ApplicationException;
import com.unipi.giguniverse.model.*;
//import com.unipi.giguniverse.repository.ConcertRepository;
//import com.unipi.giguniverse.repository.ReservationRepository;
//import com.unipi.giguniverse.repository.TicketRepository;

import com.unipi.giguniverse.repository.UserRepository;
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
//import java.sql.Date;
//import java.time.Instant;
//import java.util.ArrayList;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
//import java.util.Objects;
import java.util.Optional;

//import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class TicketService {

    /**LoadBalanced Annotation at WebClient.Builder:
     @see com.unipi.giguniverse.GiguniverseApplication
     */
//    private final String TICKET_BASE_URI = "lb://localhost:8083/api/ticket";
    private final String TICKET_BASE_URI = "lb://gu-ticket-microservice/api/ticket";

    //Resilience4j configuration
    private static final String TICKET_MICROSERVICE = "TicketMicroservice";

//    private final TicketRepository ticketRepository;
//    private final ReservationRepository reservationRepository;
//    private final ConcertRepository concertRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
//    private final MailService mailService;
    private final QRGeneratorService qrGeneratorService;
    private final ConcertService concertService;
    // Microservices version addition
    private WebClient.Builder webClientBuilder;

//    private TicketDto mapTicketToDto(Ticket ticket){
//        return TicketDto.builder()
//                .ticketId(ticket.getTicketId())
//                .ticketHolder(ticket.getTicketHolder())
//                .ticketHolderEmail(ticket.getTicketHolderEmail())
//                .ticketBuyerId(authService.getCurrentUserDetails().getUserId())
//                .concertId(ticket.getReservation().getConcert().getConcertId())
//                .concert(concertService.mapConcertToDto(ticket.getReservation().getConcert()))
//                .price(ticket.getPrice())
//                .purchaseDate(ticket.getPurchaseDate())
//                .phone(ticket.getPhone())
//                .qrcode(qrGeneratorService.generateQRCodeImageToString(ticket))
//                .build();
//    }
//
//    private Ticket mapTicketDto(TicketDto ticketDto){
//        return Ticket.builder()
//                .ticketHolder(ticketDto.getTicketHolder())
//                .ticketHolderEmail(ticketDto.getTicketHolderEmail())
//                .price(ticketDto.getPrice())
//                .phone(ticketDto.getPhone())
//                .build();
//    }

//    public TicketDto addTicket(TicketDto ticketDto){
//        Concert concert = concertRepository.getOne(ticketDto.getConcertId());
//        int reservationId = Objects.requireNonNull(concert.getReservation()).getReservationId();
//        Reservation reservation = reservationRepository.getOne(reservationId);
//
//        //Buy Ticket if there is any
//        if (checkAvailabilityAndBuyTicket(reservation.getReservationId())){
//            Ticket ticket = mapTicketDto(ticketDto);
//            ticket.setReservation(reservation);
//            ticket.setTicketBuyer(authService.getCurrentUserDetails());
//            String ticketId = ticketRepository.save(ticket).getTicketId();
//            ticket.setPrice(reservation.getTicketPrice());
//            ticket.setPurchaseDate(Date.from(Instant.now()));
//            ticket.setTicketId(ticketId);
//            //Send mail to ticket holder
//            sendEmailToTicketHolders(ticket, generateQRCodeImageToString(ticket));
//            ticketDto = mapTicketToDto(ticket);
//            return ticketDto;
//        }
//        else throw new ApplicationException("Tickets are sold out");
//    }

    // Monolithic Implementation
//    public List<TicketDto> addTickets(List<TicketDto> ticketDtos){
//
//        Concert concert = concertRepository.getOne(ticketDtos.get(0).getConcertId());
//        int reservationId = Objects.requireNonNull(concert.getReservation()).getReservationId();
//        Reservation reservation = reservationRepository.getOne(reservationId);
//
//        List<TicketDto> tickets = new ArrayList<>();
//        if(ticketDtos.size() <= reservation.getTicketNumber()){
//            for(TicketDto ticketDto: ticketDtos){
//                //Buy Ticket if there is any
//                if (checkAvailabilityAndBuyTicket(reservation.getReservationId())){
//                    Ticket ticket = mapTicketDto(ticketDto);
//                    ticket.setReservation(reservation);
//                    ticket.setTicketBuyer(authService.getCurrentUserDetails());
//                    String ticketId = ticketRepository.save(ticket).getTicketId();
//                    ticket.setPrice(reservation.getTicketPrice());
//                    ticket.setPurchaseDate(Date.from(Instant.now()));
//                    ticket.setTicketId(ticketId);
//                    //Send mail to ticket holder
//                    sendEmailToTicketHolders(ticket, qrGeneratorService.generateQRCodeImageToString(ticket));
//                    ticketDto = mapTicketToDto(ticket);
//                    tickets.add(ticketDto);
//                }
//                else throw new ApplicationException("Tickets are sold out");
//            }
//        }
//        else throw new ApplicationException("Not enough tickets available");
//        return tickets;
//    }

    // Microservices Implementation
    @RateLimiter(name = TICKET_MICROSERVICE)
    @Retry(name = TICKET_MICROSERVICE, fallbackMethod = "retryLog")
    @CircuitBreaker(name = TICKET_MICROSERVICE, fallbackMethod = "circuitBreakerLog")
    public List<TicketDto> addTickets(List<TicketDto> ticketDtos){

        User user = authService.getCurrentUserDetails();
        AttendantDto attendantDto = AttendantDto.builder()
                .userId(user.getUserId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .created(user.getCreated())
                .build();

        ticketDtos.forEach(ticketDto -> ticketDto.setTicketBuyer(attendantDto));

        ParameterizedTypeReference<List<TicketDto>> parameterizedTypeReference =
                new ParameterizedTypeReference<List<TicketDto>>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        };

        Mono<List<TicketDto>> response = webClientBuilder.build()
                .post()
                .uri(TICKET_BASE_URI)
                .body(Mono.just(ticketDtos), parameterizedTypeReference)
                .retrieve()
                .bodyToMono(parameterizedTypeReference);

        return response.block();
    }

    // Monolithic Implementation
//    public TicketDto getTicketById(String id){
//        Optional<Ticket> ticket = ticketRepository.findById(id);
//        TicketDto ticketDto=mapTicketToDto(ticket.orElseThrow(()->new ApplicationException("Ticket not found")));
//        return ticketDto;
//    }

    // Microservices Implementation
    @RateLimiter(name = TICKET_MICROSERVICE)
    @Retry(name = TICKET_MICROSERVICE)
    @CircuitBreaker(name = TICKET_MICROSERVICE)
    public TicketDto getTicketById(String id){
        Mono<TicketDto> response = webClientBuilder.build()
                .get()
                .uri(TICKET_BASE_URI +"/" +id)
                .retrieve()
                .bodyToMono(TicketDto.class);
        return response.block();
    }

    // Monolithic Implementation
//    public TicketDto validateTicket(String id){
//        Optional<Ticket> optTicket = ticketRepository.findById(id);
//        Ticket ticket = optTicket.orElseThrow(()->new ApplicationException("Ticket not found"));
//        return  TicketDto.builder()
//                .ticketId(ticket.getTicketId())
//                .ticketHolder(ticket.getTicketHolder())
//                .ticketHolderEmail(ticket.getTicketHolderEmail())
//                .concertId(ticket.getReservation().getConcert().getConcertId())
//                .concert(concertService.mapConcertToDto(ticket.getReservation().getConcert()))
//                .price(ticket.getPrice())
//                .purchaseDate(ticket.getPurchaseDate())
//                .phone(ticket.getPhone())
//                .qrcode(qrGeneratorService.generateQRCodeImageToString(ticket))
//                .build();
//    }

    // Microservices Implementation
    @RateLimiter(name = TICKET_MICROSERVICE)
    @Retry(name = TICKET_MICROSERVICE)
    @CircuitBreaker(name = TICKET_MICROSERVICE)
    public TicketDto validateTicket(String id){

        Mono<TicketDto> response = webClientBuilder.build()
                .get()
                .uri(TICKET_BASE_URI +"/validate/" +id)
                .retrieve()
                .bodyToMono(TicketDto.class);

        return response.block();
    }

    // Monolithic Implementation
//    public List<TicketDto> getAllTickets(){
//        List<TicketDto> tickets = ticketRepository.findAll()
//                .stream()
//                .map(this::mapTicketToDto)
//                .collect(toList());
//        return tickets;
//    }

    // Microservices Implementation
    @RateLimiter(name = TICKET_MICROSERVICE)
    @Retry(name = TICKET_MICROSERVICE, fallbackMethod = "retryLog")
    @CircuitBreaker(name = TICKET_MICROSERVICE, fallbackMethod = "circuitBreakerLog")
    public List<TicketDto> getAllTickets(){
        Mono<List<TicketDto>> response = webClientBuilder.build()
                .get()
                .uri(TICKET_BASE_URI)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<TicketDto>>() {
                });
        return response.block();
    }

    // Monolithic Implementation
//    public List<TicketDto> getTicketsByConcertID(Integer concertId){
//        List<TicketDto> tickets = ticketRepository.findByReservationConcertConcertId(concertId)
//                .stream()
//                .map(this::mapTicketToDto)
//                .collect(toList());
//        return tickets;
//    }

    // Microservices Implementation
    @RateLimiter(name = TICKET_MICROSERVICE)
    @Retry(name = TICKET_MICROSERVICE, fallbackMethod = "retryLog")
    @CircuitBreaker(name = TICKET_MICROSERVICE, fallbackMethod = "circuitBreakerLog")
    public List<TicketDto> getTicketsByConcertID(Integer concertId){
        Mono<List<TicketDto>> response = webClientBuilder.build()
                .get()
                .uri(TICKET_BASE_URI +"/concert/" +concertId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<TicketDto>>() {
                });
        return response.block();
    }

    // Monolithic Implementation
//    public List<TicketDto> getTicketsByTicketHolderEmail(String ticketHolderEmail){
//        List<TicketDto> tickets = ticketRepository.findByTicketHolderEmail(ticketHolderEmail)
//                .stream()
//                .map(this::mapTicketToDto)
//                .collect(toList());
//        return tickets;
//    }

    // Microservices Implementation
    @RateLimiter(name = TICKET_MICROSERVICE)
    @Retry(name = TICKET_MICROSERVICE, fallbackMethod = "retryLog")
    @CircuitBreaker(name = TICKET_MICROSERVICE, fallbackMethod = "circuitBreakerLog")
    public List<TicketDto> getTicketsByTicketHolderEmail(String ticketHolderEmail){
        Mono<List<TicketDto>> response = webClientBuilder.build()
                .get()
                .uri(TICKET_BASE_URI +"/ticketHolderEmail/" +ticketHolderEmail)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<TicketDto>>() {
                });
        return response.block();
    }

    // Monolithic Implementation
//    public List<TicketDto> getTicketsByLoggedInUser(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        org.springframework.security.core.userdetails.User principal =
//                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
//        Optional<User> user =  userRepository.findByEmail(principal.getUsername());
//        //List<TicketDto> tickets = ticketRepository.findByTicketHolderEmail (user.get().getEmail())
//        List<TicketDto> tickets = ticketRepository.findByTicketBuyerUserId(user.get().getUserId())
//                .stream()
//                .map(this::mapTicketToDto)
//                .collect(toList());
//        return tickets;
//    }

    // Microservices Implementation
    @RateLimiter(name = TICKET_MICROSERVICE)
    @Retry(name = TICKET_MICROSERVICE, fallbackMethod = "retryLog")
    @CircuitBreaker(name = TICKET_MICROSERVICE, fallbackMethod = "circuitBreakerLog")
    public List<TicketDto> getTicketsByLoggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        Optional<User> user =  userRepository.findByEmail(principal.getUsername());

        Mono<List<TicketDto>> response = webClientBuilder.build()
                .get()
                .uri(TICKET_BASE_URI +"/loggedin/" +user.get().getUserId())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<TicketDto>>() {
                });
        return response.block();
    }

    // Monolithic Implementation
//    public TicketDto updateTicket(TicketDto ticketDto){
//        Ticket existingTicket = ticketRepository.getOne(ticketDto.getTicketId());
//        existingTicket.setTicketHolder(ticketDto.getTicketHolder());
//        existingTicket.setTicketHolderEmail(ticketDto.getTicketHolderEmail());
//        existingTicket.setPrice(ticketDto.getPrice());
//        existingTicket.setPhone(ticketDto.getPhone());
//        ticketRepository.save(existingTicket);
//        return mapTicketToDto(existingTicket);
//    }

    // Microservices Implementation
    @RateLimiter(name = TICKET_MICROSERVICE)
    @Retry(name = TICKET_MICROSERVICE)
    @CircuitBreaker(name = TICKET_MICROSERVICE)
    public TicketDto updateTicket(TicketDto ticketDto){
        Mono<TicketDto> response = webClientBuilder.build()
                .put()
                .uri(TICKET_BASE_URI)
                .body(Mono.just(ticketDto), TicketDto.class)
                .retrieve()
                .bodyToMono(TicketDto.class);
        return response.block();
    }

    // Monolithic Implementation
//    public String deleteTicket(String ticketId) {
//        ticketRepository.deleteById(ticketId);
//        return "Ticket with id:" + ticketId + " was deleted.";
//    }

    // Microservices Implementation
    @RateLimiter(name = TICKET_MICROSERVICE)
    @Retry(name = TICKET_MICROSERVICE)
    @CircuitBreaker(name = TICKET_MICROSERVICE)
    public String deleteTicket(String ticketId) {
        Mono<String> response = webClientBuilder.build()
                .delete()
                .uri(TICKET_BASE_URI +"/delete/" +ticketId)
                .retrieve()
                .bodyToMono(String.class);
        return response.block();
    }

    // Method Implemented in Tickets Microservice
//    private boolean checkAvailabilityAndBuyTicket(Integer reservationId){
//        Reservation reservation = reservationRepository.getOne(reservationId);
//        if (reservation.getTicketNumber() > 0){
//            //Reduce ticket count
//            reservation.setTicketNumber(reservation.getTicketNumber()-1);
//            return true;
//        }
//        else return false;
//    }

    // Method Implemented in Tickets Microservice
//    private void sendEmailToTicketHolders(Ticket ticket, String qrString){
//        //generateQRCodeImage(ticket);
//        mailService.sendTicketEMail(ticket, qrString);
//    }

/*    public List<TicketDto> getTicketsByAttendant(Attendant ticketBuyer){
        List<TicketDto> tickets = ticketRepository.findByAttendant(ticketBuyer)
                .stream()
                .map(this::mapTicketToDto)
                .collect(Collectors.toList());
        return tickets;
    }*/

    // Catch only CallNotPermittedException Exception, unless will log all exceptions
    public List<TicketDto> circuitBreakerLog(CallNotPermittedException ex){
        log.warn("CircuitBreaker Open!!!");
        return null;
    }

    public List<TicketDto> retryLog(Throwable ex) {
        log.warn("Retry Triggered!!!");
        return null;
    }
}
