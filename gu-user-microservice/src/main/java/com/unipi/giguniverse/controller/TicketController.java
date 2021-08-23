package com.unipi.giguniverse.controller;

import com.unipi.giguniverse.dto.TicketDto;
import com.unipi.giguniverse.model.Ticket;
import com.unipi.giguniverse.service.TicketService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("api/ticket")
@AllArgsConstructor
public class TicketController {

    private final TicketService ticketService;

//    @PostMapping
//    public ResponseEntity<TicketDto> addTicket(@RequestBody TicketDto ticketDto){
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(ticketService.addTicket(ticketDto));
//    }

    @PostMapping
    public ResponseEntity<List<TicketDto>> addTickets(@RequestBody List<TicketDto> ticketDtos){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticketService.addTickets(ticketDtos));
    }

    @GetMapping
    public ResponseEntity<List<TicketDto>> getAllTickets(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getTicketById(@PathVariable String id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ticketService.getTicketById(id));
    }

    @GetMapping("/loggedin")
    public ResponseEntity<List<TicketDto>> getTicketsByLoggedInUser(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ticketService.getTicketsByLoggedInUser());
    }

    @GetMapping("/concert/{id}")
    public ResponseEntity<List<TicketDto>> getTicketsByConcertID(@PathVariable Integer id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ticketService.getTicketsByConcertID(id));
    }

    @GetMapping("/validate/{uuid}")
    public ResponseEntity<TicketDto> validate(@PathVariable String uuid){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ticketService.validateTicket(uuid));
    }

    @GetMapping("/ticketHolderEmail/{ticketHolderEmail}")
    public ResponseEntity<List<TicketDto>> getTicketByEmail(@PathVariable String ticketHolderEmail){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ticketService.getTicketsByTicketHolderEmail(ticketHolderEmail));
    }

    @PutMapping("/update")
    public ResponseEntity<TicketDto> updateTicket(@RequestBody TicketDto ticketDto){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ticketService.updateTicket(ticketDto));
    }

    @DeleteMapping("/delete/{ticketId}")
    public ResponseEntity<String> deleteTicket(@PathVariable String ticketId){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ticketService.deleteTicket(ticketId));
    }
  /*  @GetMapping("/attendants/{attendant}")
    public ResponseEntity<List<TicketDto>> getTicketByAttendant(@PathVariable Attendant attendant){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ticketService.getTicketsByAttendant(attendant));
    }*/
}
