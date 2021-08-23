package com.unipi.giguniverse.service;

import com.unipi.giguniverse.exceptions.ApplicationException;
import com.unipi.giguniverse.model.NotificationEmail;
import com.unipi.giguniverse.model.Reservation;
import com.unipi.giguniverse.model.Ticket;
import com.unipi.giguniverse.repository.TicketRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final MailContentBuilder mailContentBuilder;

    @Async
    public void sendTicketEMail(Ticket ticket, String qrString){
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("giguniverse@email.com");
            messageHelper.setTo(ticket.getTicketHolderEmail());
            messageHelper.setSubject("Αγορά Εισιτηρίου απο GigUniverse");
            messageHelper.setText(mailContentBuilder.buildTicketMail(ticket, qrString),true);
        };

        try{
            mailSender.send(messagePreparator);
            log.info("Ticket Email sent!");
        }
        catch (MailException e){
            throw new ApplicationException("Exception occurred! Failed to send the" +
                    " email to recipient: "+ticket.getTicketHolderEmail());
        }
    }

    @Async
    public void sendActivationEMail(NotificationEmail notificationEmail){
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("giguniverse@email.com");
            messageHelper.setTo(notificationEmail.getRecipient());
            messageHelper.setSubject(notificationEmail.getSubject());
            messageHelper.setText(mailContentBuilder.buildActivationMail(notificationEmail.getBody()), true);
        };

        try{
            mailSender.send(messagePreparator);
            log.info("Activation Email sent!");
        }
        catch (MailException e){
            throw new ApplicationException("Exception occurred! Failed to send the" +
                    " email to recipient: "+notificationEmail.getRecipient());
        }
    }

    @Async
    public void rescheduleConcertNotificationEmail(Ticket ticket, String qrString){
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("giguniverse@email.com");
            messageHelper.setTo(ticket.getTicketHolderEmail());
            messageHelper.setSubject("Αλλαγή Προγγραματισμού συναυλίας - GigUniverse");
            messageHelper.setText(mailContentBuilder.buildRescheduleMail(ticket, qrString),true);
        };

        try{
            mailSender.send(messagePreparator);
            log.info("Reschedule Email sent!");
        }
        catch (MailException e){
            throw new ApplicationException("Exception occurred! Failed to send the" +
                    " email to recipient: "+ticket.getTicketHolderEmail());
        }
    }

    @Async
    public void cancelConcertNotificationEmail(Ticket ticket){
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("giguniverse@email.com");
            messageHelper.setTo(ticket.getTicketHolderEmail());
            messageHelper.setSubject("Ακύρωση συναυλίας - GigUniverse");
            messageHelper.setText(mailContentBuilder.buildCancellationEMail(ticket),true);
        };
        try{
            mailSender.send(messagePreparator);
            log.info("Cancellation Email sent!");
        }
        catch (MailException e){
            throw new ApplicationException("Exception occurred! Failed to send the" +
                    " email to recipient: "+ticket.getTicketHolderEmail());
        }
    }

}
