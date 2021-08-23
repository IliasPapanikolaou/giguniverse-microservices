package com.unipi.giguniverse.controller;

import com.unipi.giguniverse.dto.VenueDto;
import com.unipi.giguniverse.service.VenueService;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

@RestController
@Slf4j
@RequestMapping("api/venue")
@AllArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    public ResponseEntity<VenueDto> addVenue(@RequestBody VenueDto venueDto){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(venueService.addVenue(venueDto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<VenueDto>> getAllVenues(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(venueService.getAllVenues());
    }

    @GetMapping
    public ResponseEntity<List<VenueDto>> getVenuesByLoggedInOwner(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(venueService.getVenuesByLoggedInOwner());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueDto> getVenue(@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(venueService.getVenueById(id));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<VenueDto>> getAllVenuesByCity(@PathVariable String city){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(venueService.getVenueByCity(city));
    }

    @PutMapping("/update")
    public ResponseEntity<VenueDto> updateVenue(@RequestBody VenueDto venueDto){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(venueService.updateVenue(venueDto));
    }

    @DeleteMapping("/delete/{venueId}")
    public ResponseEntity<String> deleteVenue(@PathVariable Integer venueId){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(venueService.deleteVenue(venueId));
    }
}
