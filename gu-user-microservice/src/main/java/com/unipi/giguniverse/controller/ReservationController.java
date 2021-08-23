package com.unipi.giguniverse.controller;

import com.unipi.giguniverse.dto.ReservationDto;
import com.unipi.giguniverse.service.ReservationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/reservation")
@AllArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> getReservationById(@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.getReservationById(id));
    }

    @PostMapping
    public ResponseEntity<ReservationDto> addReservation(@RequestBody ReservationDto reservationDto){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationService.addReservation(reservationDto));
    }

    @GetMapping
    public ResponseEntity<List<ReservationDto>> getAllReservationsByLoggedInOwner(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.getReservationsByLoggedInOwner());
    }

    @PutMapping("/update")
    public ResponseEntity<ReservationDto> updateReservation(@RequestBody ReservationDto reservationDto){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.updateReservation(reservationDto));
    }

    @DeleteMapping("/delete/{reservationId}")
    public ResponseEntity<String> deleteReservation(@PathVariable Integer reservationId){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reservationService.deleteReservation(reservationId));
    }
}
