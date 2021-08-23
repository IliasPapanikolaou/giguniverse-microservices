package com.unipi.giguniverse.config;

import com.unipi.giguniverse.security.ApplicationUserRole;
import com.unipi.giguniverse.security.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.unipi.giguniverse.security.ApplicationUserPermission.*;
import static com.unipi.giguniverse.security.ApplicationUserRole.ATTENDANT;

@EnableWebSecurity
//@Configuration
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManagerBean() throws Exception{
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .cors()
                .and()
                .csrf().disable() //protection for session connects, safe to disable with REST
                .authorizeRequests()
                .antMatchers("/","index","/css/*","/js/*").permitAll()
                .antMatchers("/swagger-resources/**", "/swagger-ui.html", "/v2/api-docs", "/webjars/**").permitAll()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/ticket/validate/**").permitAll()
                .antMatchers("/actuator/**").permitAll()
//                .antMatchers("/api/venue/**").hasRole(ApplicationUserRole.OWNER.name())
//                .antMatchers("/api/concert/**").permitAll()
//                .antMatchers("/api/reservation/**").permitAll()
//                .antMatchers("/api/ticket/**").permitAll()
                //Venues
                .antMatchers(HttpMethod.POST ,"/api/venue/**").hasAuthority(VENUE_WRITE.getPermission())
                .antMatchers(HttpMethod.PUT ,"/api/venue/**").hasAuthority(VENUE_WRITE.getPermission())
                .antMatchers(HttpMethod.DELETE ,"/api/venue/**").hasAuthority(VENUE_WRITE.getPermission())
                //Concerts
                .antMatchers(HttpMethod.POST ,"/api/concert/**").hasAuthority(CONCERT_WRITE.getPermission())
                .antMatchers(HttpMethod.PUT ,"/api/concert/**").hasAuthority(CONCERT_WRITE.getPermission())
                .antMatchers(HttpMethod.DELETE ,"/api/concert/**").hasAuthority(CONCERT_WRITE.getPermission())
                .antMatchers(HttpMethod.GET, "/api/concert/**").permitAll()
                //Reservations
                .antMatchers(HttpMethod.POST ,"/api/reservation/**").hasAuthority(RESERVATION_WRITE.getPermission())
                .antMatchers(HttpMethod.PUT ,"/api/reservation/**").hasAuthority(RESERVATION_WRITE.getPermission())
                .antMatchers(HttpMethod.DELETE ,"/api/reservation/**").hasAuthority(RESERVATION_WRITE.getPermission())
                //Tickets
                .antMatchers(HttpMethod.POST ,"/api/ticket/**").hasAuthority(TICKET_WRITE.getPermission())
                .antMatchers(HttpMethod.PUT ,"/api/ticket/**").hasAuthority(TICKET_WRITE.getPermission())
                .antMatchers(HttpMethod.DELETE ,"/api/ticket/**").hasAuthority(TICKET_WRITE.getPermission())
                .antMatchers(HttpMethod.GET, "/api/venue/**", "/api/reservation/**"
                        ,"/api/ticket/**").hasAnyRole(ApplicationUserRole.OWNER.name(), ATTENDANT.name())
                .anyRequest()
                .authenticated();

        //Add Jwt filter
        httpSecurity
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    //Authentication manager
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
