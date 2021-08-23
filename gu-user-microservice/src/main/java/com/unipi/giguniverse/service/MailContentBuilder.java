package com.unipi.giguniverse.service;

import com.unipi.giguniverse.model.Ticket;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


@Service
public class MailContentBuilder {

    private final TemplateEngine templateEngine;
    private final String pngDataB64;
    private final ResourceLoader resourceLoader;

    MailContentBuilder(TemplateEngine templateEngine, ResourceLoader resourceLoader){
        this.templateEngine = templateEngine;
        this.resourceLoader = resourceLoader;
        this.pngDataB64 = pngToBase64Converter();
    }

//    String build(String message){
//        Context context = new Context();
//        context.setVariable("message", message);
//        return templateEngine.process("mailTemplate", context);
//    }

    String buildTicketMail(Ticket ticket, String qrString){

        Context context = new Context();
        context.setVariable("logo", pngDataB64);
        context.setVariable("name", ticket.getTicketHolder());
        context.setVariable("email", ticket.getTicketHolderEmail());
        context.setVariable("price", ticket.getPrice());
        context.setVariable("purchaseDate", ticket.getPurchaseDate());
        context.setVariable("phone", ticket.getPhone());
        context.setVariable("concert", ticket.getReservation().getConcert().getConcertName());
        context.setVariable("concertDate", ticket.getReservation().getConcert().getDate());
        context.setVariable("qrString", qrString);
        context.setVariable("serial", ticket.getTicketId());
        return templateEngine.process("ticketTemplate", context);
    }

    String buildCancellationEMail(Ticket ticket){

        Context context = new Context();
        context.setVariable("logo", pngDataB64);
        context.setVariable("concert", ticket.getReservation().getConcert().getConcertName());
        context.setVariable("concertDate", ticket.getReservation().getConcert().getDate());
        context.setVariable("venueName", ticket.getReservation().getConcert().getVenue().getVenueName());
        context.setVariable("venuePhone", ticket.getReservation().getConcert().getVenue().getPhone());
        return templateEngine.process("cancelConcertTemplate", context);
    }

    String buildActivationMail(String url){
        Context context = new Context();
        context.setVariable("url", url);
        context.setVariable("logo", pngDataB64);
        return templateEngine.process("activationTemplate", context);
    }

    String buildRescheduleMail(Ticket ticket, String qrString){

        Context context = new Context();
        context.setVariable("logo", pngDataB64);
        context.setVariable("name", ticket.getTicketHolder());
        context.setVariable("email", ticket.getTicketHolderEmail());
        context.setVariable("price", ticket.getPrice());
        context.setVariable("purchaseDate", ticket.getPurchaseDate());
        context.setVariable("phone", ticket.getPhone());
        context.setVariable("concert", ticket.getReservation().getConcert().getConcertName());
        context.setVariable("concertDate", ticket.getReservation().getConcert().getDate());
        context.setVariable("qrString", qrString);
        context.setVariable("serial", ticket.getTicketId());
        return templateEngine.process("rescheduleConcertTemplate", context);
    }

    private String pngToBase64Converter(){
        String pngDataB64 = null;
        Resource resource = resourceLoader.getResource("classpath:templates/logo.png");
        try {
            BufferedImage bufferedImage = ImageIO
//                    .read(ResourceUtils.getFile("classpath:templates/logo.png"));
                    .read(resource.getInputStream());
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            pngDataB64 = Base64Utils.encodeToString(pngData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pngDataB64;
    }
}
