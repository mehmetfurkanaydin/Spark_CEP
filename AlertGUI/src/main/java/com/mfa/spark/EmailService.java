package com.mfa.spark;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.Scanner;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.*;

/**
 * Created by mfa on 19.01.2017.
 */
public class EmailService implements Runnable {


    private String userMail;
    private String message;
    private String eventType;
    private MainWindow mainWindow;
    private JTextArea textEvents;
    private String userId;
    private JSONObject jsonValue;

    public EmailService(String userMail, String eventType, String userId, final MainWindow mainWindow) {
        this.userMail = userMail;
        this.eventType = eventType;
        this.mainWindow = mainWindow;
        this.textEvents = mainWindow.getTextEvents();
        this.userId = userId;
    }

    public EmailService(String userMail, JSONObject jsonValue, String eventType, final MainWindow mainWindow) {
        this.userMail = userMail;
        this.jsonValue = jsonValue;
        this.mainWindow = mainWindow;
        this.eventType = eventType;
        this.textEvents = mainWindow.getTextEvents();
    }

    @Override
    public void run() {
        // Recipient's email ID needs to be mentioned.
        //String to = "aramamotoru.araproje@gmail.com";//change accordingly

        String filename = null;
        File file;
        Scanner scanner;
        String eventMessage = "";

        switch (eventType) {
            case "SALE":
                filename = "./SALE.txt";
                file = new File(filename);
                scanner = null;
                try {
                    scanner = new Scanner(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                while (scanner.hasNextLine()) {
                    String msg = scanner.nextLine();
                    eventMessage = eventMessage + msg;
                }
                break;
            case "HELP":
                filename = "./HELP.txt";
                file = new File(filename);
                scanner = null;
                try {
                    scanner = new Scanner(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                while (scanner.hasNextLine()) {
                    String msg = scanner.nextLine();
                    eventMessage = eventMessage + msg;
                }
                break;
            case "cf":
                this.userId = this.jsonValue.get("ip").toString();
                String firstItems = this.jsonValue.get("currentItems").toString();
                String secondItems = this.jsonValue.get("secondItems").toString();
                String suggestedItems = ItemSimilarity.foundSuggestedItems(firstItems, secondItems);
                eventMessage = "Do you Want to see these products? = " + suggestedItems;
                break;
        }


        String to = userMail;
        // Sender's email ID needs to be mentioned
        String from = "gradproj11@gmail.com";//change accordingly
        final String username = "gradproj11";//change accordingly
        final String password = "";//change accordingly

        // Assuming you are sending email through relay.jangosmtp.net
        String host = "smtp.gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            // Set Subject: header field
            message.setSubject(eventType);

            // Now set the actual message

            message.setText(eventMessage);
            // Send message
            Transport.send(message);

            textEvents.append("Email send to = " + userId + " about event = " + eventType + "\n ==================================== \n");
            textEvents.setCaretPosition(textEvents.getText().length() - 1);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}