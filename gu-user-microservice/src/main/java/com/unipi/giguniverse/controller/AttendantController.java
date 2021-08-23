package com.unipi.giguniverse.controller;

import com.unipi.giguniverse.dto.AttendantDto;
import com.unipi.giguniverse.dto.OwnerDto;
import com.unipi.giguniverse.service.AttendantService;
import com.unipi.giguniverse.service.OwnerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("api/attendant")
@AllArgsConstructor
public class AttendantController {

    private final AttendantService attendantService;

    @GetMapping
    public ResponseEntity<AttendantDto> getAttendant(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(attendantService.getAttendant());
    }
    @PutMapping("/update")
    public ResponseEntity<AttendantDto> updateAttendant(@RequestBody AttendantDto attendantDto){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(attendantService.updateAttendant(attendantDto));
    }
}
