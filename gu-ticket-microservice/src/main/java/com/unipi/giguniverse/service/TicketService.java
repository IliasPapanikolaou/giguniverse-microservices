package com.unipi.giguniverse.service;

import com.unipi.giguniverse.dto.*;
import com.unipi.giguniverse.exceptions.ApplicationException;
import com.unipi.giguniverse.model.*;
import com.unipi.giguniverse.repository.*;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.transaction.Transactional;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class TicketService {

//    private final String CONCERT_BASE_URI = "lb://localhost:8082/api/concert";
//    private final String RESERVATION_BASE_URI = "lb://localhost:8082/api/reservation";
    private final String CONCERT_BASE_URI = "lb://gu-concert-microservice/api/concert";
    private final String RESERVATION_BASE_URI = "lb://gu-concert-microservice/api/reservation";
    private final String VENUE_BASE_URI = "lb://gu-venue-microservice/api/venue";

    //Resilience4j Configuration
    public static final String TICKET_MICROSERVICE = "TicketMicroservice";

    private final TicketRepository ticketRepository;
//    private final ReservationRepository reservationRepository;
//    private final ConcertRepository concertRepository;
//    private final UserRepository userRepository;
//    private final AuthService authService;
    private final MailService mailService;
    private final QRGeneratorService qrGeneratorService;
//    private final ConcertService concertService;
    // Microservices version addition
    private WebClient.Builder webClientBuilder;

    private TicketDto mapTicketToDto(Ticket ticket){
        return TicketDto.builder()
                .ticketId(ticket.getTicketId())
                .ticketHolder(ticket.getTicketHolder())
                .ticketHolderEmail(ticket.getTicketHolderEmail())
                //.ticketBuyerId(authService.getCurrentUserDetails().getUserId())
                .ticketBuyerId(ticket.getTicketBuyer().getUserId())
                .concertId(ticket.getReservation().getConcert().getConcertId())
                .concert(mapConcertToDto(ticket.getReservation().getConcert()))
                .price(ticket.getPrice())
                .purchaseDate(ticket.getPurchaseDate())
                .phone(ticket.getPhone())
                .qrcode(qrGeneratorService.generateQRCodeImageToString(ticket))
                .build();
    }

    private Ticket mapTicketDto(TicketDto ticketDto){
        return Ticket.builder()
                .ticketHolder(ticketDto.getTicketHolder())
                .ticketHolderEmail(ticketDto.getTicketHolderEmail())
                .price(ticketDto.getPrice())
                .phone(ticketDto.getPhone())
                .build();
    }


    // Monolithic Implementation
//    private ConcertDto mapConcertToDto(Concert concert){
//        Reservation reservation = reservationRepository
//                .getOne(Objects.requireNonNull(concert.getReservation()).getReservationId());
//
//        return ConcertDto.builder()
//                .concertId(concert.getConcertId())
//                .concertName(concert.getConcertName())
//                .description(concert.getDescription())
//                .venueId(concert.getVenue().getVenueId())
//                .venue(mapVenueToVenueDto(concert.getVenue()))
//                .date(concert.getDate())
//                .ticketNumber(reservation.getTicketNumber())
//                .ticketPrice(reservation.getTicketPrice())
//                .image(concert.getImage())
//                .build();
//    }

    // Microservices Implementation
    @Retry(name = TICKET_MICROSERVICE)
    @CircuitBreaker(name = TICKET_MICROSERVICE)
    private ConcertDto mapConcertToDto(Concert concert){

        Mono<ReservationDto> response = webClientBuilder.build()
                .get()
                .uri(RESERVATION_BASE_URI +"/" +concert.getReservation().getReservationId())
                .retrieve()
                .bodyToMono(ReservationDto.class);

        ReservationDto reservation = response.block();

        return ConcertDto.builder()
                .concertId(concert.getConcertId())
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

    // Microservices Implementation
//    @Retry(name = CONCERT_MICROSERVICE)
//    @CircuitBreaker(name = CONCERT_MICROSERVICE)
    private Concert mapConcertDto(ConcertDto concertDto){

        Mono<Reservation> response = webClientBuilder.build()
                .get()
                .uri(RESERVATION_BASE_URI +"/" + concertDto.getReservationId())
                .retrieve()
                .bodyToMono(Reservation.class);

        Mono<Venue> response2 = webClientBuilder.build()
                .get()
                .uri(VENUE_BASE_URI +"/" +concertDto.getVenueId())
                .retrieve()
                .bodyToMono(Venue.class);

        return Concert.builder()
                .concertName(concertDto.getConcertName())
                .description(concertDto.getDescription())
                .reservation(response.block())
                .venue(response2.block())
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

    // Monolithic Implementation
//    private Reservation mapReservationDto(ReservationDto reservationDto){
//        return Reservation.builder()
//                .reservationId(reservationDto.getReservationId())
//                .startingDate(reservationDto.getStartingDate())
//                .finalDate(reservationDto.getFinalDate())
//                .ticketNumber(reservationDto.getTicketNumber())
//                .ticketPrice(reservationDto.getTicketPrice())
//                .owner(reservationDto.getOwner())
//                .concert(concertRepository.getOne(reservationDto.getConcertId()))
//                .build();
//    }

    // Microservices Implementation
    @Retry(name = TICKET_MICROSERVICE)
    @CircuitBreaker(name = TICKET_MICROSERVICE)
    private Reservation mapReservationDto(ReservationDto reservationDto){

        Mono<ConcertDto> concertDtoMono = webClientBuilder.build()
                .get()
                .uri(CONCERT_BASE_URI +"/" + reservationDto.getConcertId())
                .retrieve()
                .bodyToMono(ConcertDto.class);

        ConcertDto concertDto = concertDtoMono.block();
        assert concertDto != null;

        return Reservation.builder()
                .reservationId(reservationDto.getReservationId())
                .startingDate(reservationDto.getStartingDate())
                .finalDate(reservationDto.getFinalDate())
                .ticketNumber(reservationDto.getTicketNumber())
                .ticketPrice(reservationDto.getTicketPrice())
                .owner(reservationDto.getOwner())
                .concert(mapConcertDto(concertDto))
                .build();
    }

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
    @Retry(name = TICKET_MICROSERVICE)
    @CircuitBreaker(name = TICKET_MICROSERVICE)
    public List<TicketDto> addTickets(List<TicketDto> ticketDtos){

        Mono<ConcertDto> concertDtoMono = webClientBuilder.build()
                .get()
                .uri(CONCERT_BASE_URI +"/" + ticketDtos.get(0).getConcertId())
                .retrieve()
                .bodyToMono(ConcertDto.class);

        ConcertDto concertDto = concertDtoMono.block();
        assert concertDto != null;
        int reservationId = concertDto.getReservationId();

        Mono<ReservationDto> response2 = webClientBuilder.build()
                .get()
                .uri(RESERVATION_BASE_URI +"/" +reservationId)
                .retrieve()
                .bodyToMono(ReservationDto.class);

        Reservation reservation = mapReservationDto(Objects.requireNonNull(response2.block()));

        List<TicketDto> tickets = new ArrayList<>();
        if(ticketDtos.size() <= reservation.getTicketNumber()){
            for(TicketDto ticketDto: ticketDtos){
                //Buy Ticket if there is any
                if (checkAvailabilityAndBuyTicket(reservation.getReservationId())){
                    Ticket ticket = mapTicketDto(ticketDto);
                    ticket.setReservation(reservation);
                    ticket.setTicketBuyer(Attendant.builder()
                            .userId(ticketDto.getTicketBuyer().getUserId())
                            .firstname(ticketDto.getTicketBuyer().getFirstname())
                            .lastname(ticketDto.getTicketBuyer().getLastname())
                            .email(ticketDto.getTicketBuyer().getEmail())
                            .build());
                    String ticketId = ticketRepository.save(ticket).getTicketId();
                    ticket.setPrice(reservation.getTicketPrice());
                    ticket.setPurchaseDate(Date.from(Instant.now()));
                    ticket.setTicketId(ticketId);
                    //Send mail to ticket holder
                    sendEmailToTicketHolders(ticket, qrGeneratorService.generateQRCodeImageToString(ticket));
                    ticketDto = mapTicketToDto(ticket);
                    tickets.add(ticketDto);
                }
                else throw new ApplicationException("Tickets are sold out");
            }
        }
        else throw new ApplicationException("Not enough tickets available");

        return tickets;
    }

    public TicketDto getTicketById(String id){
        Optional<Ticket> ticket = ticketRepository.findById(id);
        TicketDto ticketDto=mapTicketToDto(ticket.orElseThrow(()->new ApplicationException("Ticket not found")));
        return ticketDto;
    }

    public TicketDto validateTicket(String id){
        Optional<Ticket> optTicket = ticketRepository.findById(id);
        Ticket ticket = optTicket.orElseThrow(()->new ApplicationException("Ticket not found"));
        return  TicketDto.builder()
                .ticketId(ticket.getTicketId())
                .ticketHolder(ticket.getTicketHolder())
                .ticketHolderEmail(ticket.getTicketHolderEmail())
                .concertId(ticket.getReservation().getConcert().getConcertId())
                .concert(mapConcertToDto(ticket.getReservation().getConcert()))
                .price(ticket.getPrice())
                .purchaseDate(ticket.getPurchaseDate())
                .phone(ticket.getPhone())
                .qrcode(qrGeneratorService.generateQRCodeImageToString(ticket))
                .build();
    }

    public List<TicketDto> getAllTickets(){
        List<TicketDto> tickets = ticketRepository.findAll()
                .stream()
                .map(this::mapTicketToDto)
                .collect(toList());
        return tickets;
    }

    public List<TicketDto> getTicketsByConcertID(Integer concertId){
        List<TicketDto> tickets = ticketRepository.findByReservationConcertConcertId(concertId)
                .stream()
                .map(this::mapTicketToDto)
                .collect(toList());
        return tickets;
    }

    public List<TicketDto> getTicketsByTicketHolderEmail(String ticketHolderEmail){
        List<TicketDto> tickets = ticketRepository.findByTicketHolderEmail(ticketHolderEmail)
                .stream()
                .map(this::mapTicketToDto)
                .collect(toList());
        return tickets;
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
    public List<TicketDto> getTicketsByLoggedInUser(Integer userId){
        //List<TicketDto> tickets = ticketRepository.findByTicketHolderEmail (user.get().getEmail())
        List<TicketDto> tickets = ticketRepository.findByTicketBuyerUserId(userId)
                .stream()
                .map(this::mapTicketToDto)
                .collect(toList());
        return tickets;
    }

    public TicketDto updateTicket(TicketDto ticketDto){
        Ticket existingTicket = ticketRepository.getOne(ticketDto.getTicketId());
        existingTicket.setTicketHolder(ticketDto.getTicketHolder());
        existingTicket.setTicketHolderEmail(ticketDto.getTicketHolderEmail());
        existingTicket.setPrice(ticketDto.getPrice());
        existingTicket.setPhone(ticketDto.getPhone());
        ticketRepository.save(existingTicket);
        return mapTicketToDto(existingTicket);
    }

    public String deleteTicket(String ticketId) {
        ticketRepository.deleteById(ticketId);
        return "Ticket with id:" + ticketId + " was deleted.";
    }

    // Monolithic Implementation
//    private boolean checkAvailabilityAndBuyTicket(Integer reservationId){
//        Reservation reservation = reservationRepository.getOne(reservationId);
//
//        if (reservation.getTicketNumber() > 0){
//            //Reduce ticket count
//            reservation.setTicketNumber(reservation.getTicketNumber()-1);
//            return true;
//        }
//        else return false;
//    }

    // Microservices Implementation
    @Retry(name = TICKET_MICROSERVICE)
    @CircuitBreaker(name = TICKET_MICROSERVICE)
    private boolean checkAvailabilityAndBuyTicket(Integer reservationId){

        Mono<ReservationDto> response = webClientBuilder.build()
                .get()
                .uri(RESERVATION_BASE_URI +"/" +reservationId)
                .retrieve()
                .bodyToMono(ReservationDto.class);

        Reservation reservation = mapReservationDto(response.block());

        if (reservation.getTicketNumber() > 0){
            //Reduce ticket count
            reservation.setTicketNumber(reservation.getTicketNumber()-1);
            return true;
        }
        else return false;
    }

    private void sendEmailToTicketHolders(Ticket ticket, String qrString){
        //generateQRCodeImage(ticket);
        mailService.sendTicketEMail(ticket, qrString);
    }

/*    public List<TicketDto> getTicketsByAttendant(Attendant ticketBuyer){
        List<TicketDto> tickets = ticketRepository.findByAttendant(ticketBuyer)
                .stream()
                .map(this::mapTicketToDto)
                .collect(Collectors.toList());
        return tickets;
    }*/
}
