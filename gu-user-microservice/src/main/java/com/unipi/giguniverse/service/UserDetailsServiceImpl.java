package com.unipi.giguniverse.service;

import com.unipi.giguniverse.exceptions.ApplicationException;
import com.unipi.giguniverse.model.Owner;
import com.unipi.giguniverse.model.User;
import com.unipi.giguniverse.repository.UserRepository;
import com.unipi.giguniverse.security.ApplicationUserRole;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singletonList;

//Implementation of UserDetailsService Interface
@Service
@AllArgsConstructor
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email){
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user = userOptional.orElseThrow(()->new ApplicationException("User not found"));

        //TODO: Testing User roles
        //String role = ApplicationUserRole.ATTENDANT.name();
        Set<SimpleGrantedAuthority> authorities = ApplicationUserRole.ATTENDANT.getGrantedAuthorities();
        if (user instanceof Owner){
            //String role = ApplicationUserRole.OWNER.name();
            authorities = ApplicationUserRole.OWNER.getGrantedAuthorities();
        }
        //TODO: Remove sout
        System.out.println(authorities.toString());


        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
//                .roles(role)
                .authorities(authorities)
                .build();

        return userDetails;
    }
}
