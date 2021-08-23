package com.unipi.giguniverse.controller;

import com.unipi.giguniverse.dto.VenueDto;
import com.unipi.giguniverse.service.VenueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.status;

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

//    @GetMapping
//    public ResponseEntity<List<VenueDto>> getVenuesByLoggedInOwner(){
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(venueService.getVenuesByLoggedInOwner());
//    }

    // Microservices Implementation
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VenueDto>> getVenuesByLoggedInOwner(@PathVariable Integer userId){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(venueService.getVenuesByLoggedInOwner(userId));
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
