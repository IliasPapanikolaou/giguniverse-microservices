package com.unipi.giguniverse.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.unipi.giguniverse.model.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

@Service
public class QRGeneratorService {

    public void generateQRCodeImage(String text, int width, int height, String filePath){
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }

    public String getQRCodeImageAsBase64(String text, int width, int height){
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        String pngDataB64 = null;
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            pngDataB64 = Base64Utils.encodeToString(pngData);

        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
        return pngDataB64;
    }

    public String generateQRCodeImageToString(Ticket ticket){
        String qrText = ticket.getTicketId() + "\n" +
                ticket.getReservation().getConcert().getConcertName() + "\n" +
                ticket.getTicketHolder() + "\n" +
                ticket.getTicketHolderEmail() + "\n" +
                ticket.getPurchaseDate();
        return getQRCodeImageAsBase64(qrText,150, 150);
    }
}
