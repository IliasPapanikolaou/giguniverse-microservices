package com.unipi.giguniverse.service;

//import com.unipi.giguniverse.config.Resilience4jConfig;
import com.unipi.giguniverse.dto.ConcertDto;
import com.unipi.giguniverse.dto.VenueDto;
import com.unipi.giguniverse.model.Owner;
import com.unipi.giguniverse.model.User;
import com.unipi.giguniverse.model.Venue;
import com.unipi.giguniverse.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class VenueService {

    /**LoadBalanced Annotation at WebClient.Builder:
      @see com.unipi.giguniverse.GiguniverseApplication
    */
//    private final String VENUE_BASE_URI = "lb://localhost:8081/api/venue";
    private final String VENUE_BASE_URI = "lb://gu-venue-microservice/api/venue";

    //Resilience4j configuration
    private static final String VENUE_MICROSERVICE = "VenueMicroservice";

//    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    // Microservices version addition
    private WebClient.Builder webClientBuilder;
//    private ReactiveCircuitBreakerFactory rcbFactory;

    // Monolithic Implementation
//    public VenueDto addVenue(VenueDto venueDto){
//        //Check by VenueName if venue already exists in DB
//        if(!venueRepository.existsVenueByVenueName(venueDto.getVenueName())){
//            //Add venue to DB
//            int venueId = venueRepository.save(mapVenueDtoToVenue(venueDto)).getVenueId();
//            venueDto.setVenueId(venueId);
//            return venueDto;
//        }
//        else {
//            throw new ApplicationException("Venue already exists");
//        }
//    }

    // Microservices Implementation
    @RateLimiter(name = VENUE_MICROSERVICE)
    @Retry(name = VENUE_MICROSERVICE)
    @CircuitBreaker(name = VENUE_MICROSERVICE)
    public VenueDto addVenue(VenueDto venueDto){
        Mono<VenueDto> response = webClientBuilder.build()
                .post()
                .uri (VENUE_BASE_URI)
                .body(Mono.just(mapVenueDtoToVenue(venueDto)), VenueDto.class)
                .retrieve()
                .bodyToMono(VenueDto.class);

        return response.block();
    }

    // Monolithic Implementation
//    public List<VenueDto> getAllVenues(){
//        List<VenueDto> venues = venueRepository.findAll()
//                .stream()
//                .map(this::mapVenueToVenueDto)
//                .collect(toList());
//        return venues;
//    }

    // Microservices Implementation
    @RateLimiter(name = VENUE_MICROSERVICE)
    @Retry(name = VENUE_MICROSERVICE, fallbackMethod = "retryLog")
    @CircuitBreaker(name = VENUE_MICROSERVICE, fallbackMethod = "circuitBreakerLog")
    public List<VenueDto> getAllVenues(){
        // Use ParameterizedTypeReference to receive the List
        Mono<List<VenueDto>> response = webClientBuilder.build()
                .get()
                .uri(VENUE_BASE_URI +"/all")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<VenueDto>>() {
                });
        // response to venue
        // convert to venueDto and return
        return response.block();
    }

    VenueDto mapVenueToVenueDto(Venue venue){
        return VenueDto.builder()
                .venueId(venue.getVenueId())
                .venueName(venue.getVenueName())
                .address(venue.getAddress())
                .city(venue.getCity())
                .phone(venue.getPhone())
                .capacity(venue.getCapacity())
                .build();
    }

    public Venue mapVenueDtoToVenue(VenueDto venueDto) {
        return Venue.builder()
                .venueName(venueDto.getVenueName())
                .owner((Owner) authService.getCurrentUserDetails())
                .address(venueDto.getAddress())
                .city(venueDto.getCity())
                .phone(venueDto.getPhone())
                .capacity(venueDto.getCapacity())
                .build();
    }

    // Monolithic Implementation
//    public VenueDto getVenueById(Integer id){
//        Optional<Venue> venue =  venueRepository.findById(id);
//        VenueDto venueDto = mapVenueToVenueDto(venue.orElseThrow(()->new ApplicationException("Venue not found")));
//        return venueDto;
//    }

    // Microservices Implementation - Reactive programming
    @RateLimiter(name = VENUE_MICROSERVICE)
    @Retry(name = VENUE_MICROSERVICE)
    @CircuitBreaker(name = VENUE_MICROSERVICE)
    public VenueDto getVenueById(Integer id){
        Mono<VenueDto> response = webClientBuilder.build()
                .get()
                .uri(VENUE_BASE_URI +"/" +id)
                .retrieve()
                .bodyToMono(VenueDto.class);

        return response.block();
    }

    // Monolithic Implementation
//    public List<VenueDto> getVenuesByLoggedInOwner(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        org.springframework.security.core.userdetails.User principal =
//                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
//        Optional<User> owner =  userRepository.findByEmail(principal.getUsername());
//        List<VenueDto> venues = venueRepository.findByOwnerUserId(owner.get().getUserId())
//                .stream()
//                .map(this::mapVenueToVenueDto)
//                .collect(toList());
//        return venues;
//    }

    //Older Resilience4j CircuitBreaker Implementation - Microservices Implementation
//    public List<VenueDto> getVenuesByLoggedInOwner(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        org.springframework.security.core.userdetails.User principal =
//                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
//        Optional<User> owner =  userRepository.findByEmail(principal.getUsername());
//
//        List<VenueDto> fallback = new ArrayList<>();
//        fallback.add(VenueDto.builder().venueName("Fallback - Circuit Breaker").build());
//
//        // Use ParameterizedTypeReference to receive the List
//        Mono<List<VenueDto>> response = webClientBuilder.build()
//                .get()
//                .uri(VENUE_BASE_URI +"/user/" +owner.get().getUserId())
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<List<VenueDto>>() {})
//                .transform(rcb -> rcbFactory.create("circuitbreaker").run(rcb, throwable -> {
//                    System.out.println("Circuit Breaker Triggered!!!!");
//                    return Mono.just(fallback);
//                }));
//        // response to venue
//        return response.block();
//    }


    @RateLimiter(name = VENUE_MICROSERVICE)
    @Retry(name = VENUE_MICROSERVICE, fallbackMethod = "retryLog")
    @CircuitBreaker(name = VENUE_MICROSERVICE, fallbackMethod = "circuitBreakerLog")
    public List<VenueDto> getVenuesByLoggedInOwner(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        Optional<User> owner =  userRepository.findByEmail(principal.getUsername());

        // Use ParameterizedTypeReference to receive the List
        Mono<List<VenueDto>> response = webClientBuilder.build()
                .get()
                .uri(VENUE_BASE_URI +"/user/" +owner.get().getUserId())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<VenueDto>>() {});
        // response to venue
        return response.block();
    }

    // Monolithic Implementation
//    public List<VenueDto> getVenueByCity(String city){
//        List<VenueDto> venues = venueRepository.findAllByCity(city)
//                .stream()
//                .map(this::mapVenueToVenueDto)
//                .collect(toList());
//        return venues;
//    }

    // Microservices Implementation - Reactive programming
    @RateLimiter(name = VENUE_MICROSERVICE)
    @Retry(name = VENUE_MICROSERVICE, fallbackMethod = "retryLog")
    @CircuitBreaker(name = VENUE_MICROSERVICE, fallbackMethod = "circuitBreakerLog")
    public List<VenueDto> getVenueByCity(String city){
        // Use ParameterizedTypeReference to receive the List
        Mono<List<VenueDto>> response = webClientBuilder.build()
                .get()
                .uri(VENUE_BASE_URI +"/city/" +city)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<VenueDto>>() {
                });
        // response to venue
        return response.block();
    }

    // Monolithic PUT Implementation
//    public VenueDto updateVenue(VenueDto venueDto){
//        Venue existingVenue = venueRepository.getOne(venueDto.getVenueId());
//        existingVenue.setVenueName(venueDto.getVenueName());
//        existingVenue.setAddress(venueDto.getAddress());
//        existingVenue.setCity(venueDto.getCity());
//        existingVenue.setPhone(venueDto.getPhone());
//        existingVenue.setCapacity(venueDto.getCapacity());
//        venueRepository.save(existingVenue);
//        return mapVenueToVenueDto(existingVenue);
//    }

    // Microservices PUT Implementation - Reactive programming
    @RateLimiter(name = VENUE_MICROSERVICE)
    @Retry(name = VENUE_MICROSERVICE)
    @CircuitBreaker(name = VENUE_MICROSERVICE)
    public VenueDto updateVenue(VenueDto venueDto){

        Mono<VenueDto> response = webClientBuilder.build()
                .put()
                .uri(VENUE_BASE_URI +"/update")
                .body(Mono.just(venueDto), VenueDto.class)
                .retrieve()
                .bodyToMono(VenueDto.class);

        return  response.block();
    }

    // Monolithic DELETE Implementation
//    public String deleteVenue(Integer venueId){
//        venueRepository.deleteById(venueId);
//        return "Venue with id:" + venueId.toString() + " was deleted.";
//    }

    // Microservices PUT Implementation - Reactive programming
    @RateLimiter(name = VENUE_MICROSERVICE)
    @Retry(name = VENUE_MICROSERVICE)
    @CircuitBreaker(name = VENUE_MICROSERVICE)
    public String deleteVenue(Integer venueId){
        Mono<Venue> response = webClientBuilder.build()
                .delete()
                .uri(VENUE_BASE_URI +"/delete/" +venueId)
                .retrieve()
                .bodyToMono(Venue.class);

        Boolean isDeleted = response.subscribe().isDisposed();
        if(isDeleted) return "Venue with id:" + venueId + " was deleted.";
        else return "Venue not found";
    }

    // Catch only CallNotPermittedException Exception, unless will log all exceptions
    public List<VenueDto> circuitBreakerLog(CallNotPermittedException ex){
        log.warn("CircuitBreaker Open!!!");
        return null;
    }

    public List<VenueDto> retryLog(Throwable ex) {
        log.warn("Retry Triggered!!!");
        return null;
    }
}
