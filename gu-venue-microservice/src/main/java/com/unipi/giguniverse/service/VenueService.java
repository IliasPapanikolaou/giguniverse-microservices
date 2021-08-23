package com.unipi.giguniverse.service;

import com.unipi.giguniverse.dto.VenueDto;
import com.unipi.giguniverse.exceptions.ApplicationException;
import com.unipi.giguniverse.model.Owner;
import com.unipi.giguniverse.model.User;
import com.unipi.giguniverse.model.Venue;
//import com.unipi.giguniverse.repository.UserRepository;
import com.unipi.giguniverse.repository.VenueRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class VenueService {

    private final VenueRepository venueRepository;
//    private final UserRepository userRepository;
//    private final AuthService authService;


    public VenueDto addVenue(VenueDto venueDto){
        //Check by VenueName if venue already exists in DB
        if(!venueRepository.existsVenueByVenueName(venueDto.getVenueName())){
            //Add venue to DB
            int venueId = venueRepository.save(mapVenueDtoToVenue(venueDto)).getVenueId();
            venueDto.setVenueId(venueId);
            return venueDto;
        }
        else {
            throw new ApplicationException("Venue already exists");
        }
    }

    public List<VenueDto> getAllVenues(){
        List<VenueDto> venues = venueRepository.findAll()
                .stream()
                .map(this::mapVenueToVenueDto)
                .collect(toList());
        return venues;
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

    private Venue mapVenueDtoToVenue(VenueDto venueDto) {
        return Venue.builder()
                .venueName(venueDto.getVenueName())
//                .owner((Owner) authService.getCurrentUserDetails())
                // Microservices addition instead of current users ^^
                .owner(venueDto.getOwner())
                .address(venueDto.getAddress())
                .city(venueDto.getCity())
                .phone(venueDto.getPhone())
                .capacity(venueDto.getCapacity())
                .build();
    }

    public VenueDto getVenueById(Integer id){
        Optional<Venue> venue =  venueRepository.findById(id);
        VenueDto venueDto = mapVenueToVenueDto(venue.orElseThrow(()->new ApplicationException("Venue not found")));
        return venueDto;
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

    // Microservices Implementation
    public List<VenueDto> getVenuesByLoggedInOwner(Integer userId){
        List<VenueDto> venues = venueRepository.findByOwnerUserId(userId)
                .stream()
                .map(this::mapVenueToVenueDto)
                .collect(toList());
        return venues;
    }

    public List<VenueDto> getVenueByCity(String city){
        List<VenueDto> venues = venueRepository.findAllByCity(city)
                .stream()
                .map(this::mapVenueToVenueDto)
                .collect(toList());
        return venues;
    }

    public VenueDto updateVenue(VenueDto venueDto){
        Venue existingVenue = venueRepository.getOne(venueDto.getVenueId());
        existingVenue.setVenueName(venueDto.getVenueName());
        existingVenue.setAddress(venueDto.getAddress());
        existingVenue.setCity(venueDto.getCity());
        existingVenue.setPhone(venueDto.getPhone());
        existingVenue.setCapacity(venueDto.getCapacity());
        venueRepository.save(existingVenue);
        return mapVenueToVenueDto(existingVenue);
    }

    public String deleteVenue(Integer venueId){
        venueRepository.deleteById(venueId);
        return "Venue with id:" + venueId.toString() + " was deleted.";
    }

}
