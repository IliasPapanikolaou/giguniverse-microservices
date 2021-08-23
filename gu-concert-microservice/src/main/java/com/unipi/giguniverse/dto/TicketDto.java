package com.unipi.giguniverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDto {

    private String ticketId;
    @NotBlank(message = "Concert ID Required")
    private int concertId;
    private ConcertDto concert;
    @NotBlank(message = "Ticket Holder Name Required")
    private String ticketHolder;
    @NotBlank(message = "Ticket Holder Email Required")
    @Email
    private String ticketHolderEmail;
    //private TicketType ticketType;
    private double price;
    @NotBlank(message = "Ticket Buyer ID Required")
    private int ticketBuyerId;
    private String phone;
    private Date purchaseDate;
    private String qrcode;
}
