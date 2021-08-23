package com.unipi.giguniverse.controller;

import com.unipi.giguniverse.dto.ConcertDto;
import com.unipi.giguniverse.model.Venue;
import com.unipi.giguniverse.service.ConcertService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/concert")
@AllArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @PostMapping
    public ResponseEntity<ConcertDto> addConcert(@RequestBody ConcertDto concertDto){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(concertService.addConcertAndReservation(concertDto));
                //.body(concertService.addConcert(concertDto));
    }

    @GetMapping
    public ResponseEntity<List<ConcertDto>> getAllConcerts(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(concertService.getAllConcerts());
    }

    @GetMapping("/owner")
    public ResponseEntity<List<ConcertDto>> getAllConcertsByLoggedInOwner(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(concertService.getConcertByLoggedInOwner());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConcertDto> getConcertById(@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(concertService.getConcertById(id));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<ConcertDto>> getConcertByDate(@PathVariable @DateTimeFormat(pattern="yyyy-MM-dd")LocalDate date){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(concertService.getConcertByDate(date));
    }

    @GetMapping("/month/{date}")
    public ResponseEntity<List<ConcertDto>> getConcertByMonth(@PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate date){
        System.out.println(date);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(concertService.getConcertByMonth(date));
    }

    @GetMapping("/venue/{venue}")
    public ResponseEntity<List<ConcertDto>> getConcertByVenue(@PathVariable Venue venue){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(concertService.getConcertByVenue(venue));
    }

    @PutMapping("/update")
    public ResponseEntity<ConcertDto> updateConcert(@RequestBody ConcertDto concertDto){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(concertService.updateConcert(concertDto));
    }

    @DeleteMapping("/delete/{concertId}")
    public ResponseEntity<String> deleteConcert(@PathVariable Integer concertId){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(concertService.deleteConcert(concertId));
    }
}
