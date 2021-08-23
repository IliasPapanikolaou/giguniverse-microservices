package com.unipi.giguniverse.service;

import com.unipi.giguniverse.dto.AttendantDto;
import com.unipi.giguniverse.exceptions.ApplicationException;
import com.unipi.giguniverse.model.Attendant;
import com.unipi.giguniverse.model.User;
import com.unipi.giguniverse.repository.AttendantRepository;
import com.unipi.giguniverse.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class AttendantService {
    private final UserRepository userRepository;
    private final AttendantRepository attendantRepository;

    private AttendantDto mapAttendantToAttendantDto(Attendant attendant){
        return AttendantDto.builder()
                .userId(attendant.getUserId())
                .firstname(attendant.getFirstname())
                .lastname(attendant.getLastname())
                .email(attendant.getEmail())
                .created(attendant.getCreated())
                .image(attendant.getImage())
                .build();
    }

    public AttendantDto getAttendant(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        Optional<User> attendant =  userRepository.findByEmail(principal.getUsername());
        AttendantDto attendantDto = mapAttendantToAttendantDto((Attendant) attendant.
                orElseThrow(()->new ApplicationException("Attendant not found")));
        return attendantDto;
    }

    public AttendantDto updateAttendant(AttendantDto attendantDto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        Optional<Attendant> existingAttendant =  attendantRepository.findByEmail(principal.getUsername());
        existingAttendant.get().setFirstname(attendantDto.getFirstname());
        existingAttendant.get().setLastname(attendantDto.getLastname());
        existingAttendant.get().setImage(attendantDto.getImage());
        return mapAttendantToAttendantDto(existingAttendant.get());
    }

}
