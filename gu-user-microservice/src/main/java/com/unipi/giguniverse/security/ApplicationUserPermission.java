package com.unipi.giguniverse.security;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ApplicationUserPermission {
    VENUE_READ("venue:read"),
    VENUE_WRITE("venue:write"),
    CONCERT_READ("concert:read"),
    CONCERT_WRITE("concert:write"),
    TICKET_READ("ticket:read"),
    TICKET_WRITE("ticket:write"),
    RESERVATION_READ("reservation:read"),
    RESERVATION_WRITE("reservation:write");

    private final String permission;

    public String getPermission(){
        return permission;
    }

}
