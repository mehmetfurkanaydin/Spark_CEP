package com.mfa.spark;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by mfa on 19.01.2017.
 */
public class SMSService implements Runnable {
    private String userPhoneNumber;
    private String message;
    private String eventType;
    private MainWindow mainWindow;
    private JTextArea textEvents;
    private String userId;
    private JSONObject jsonValue;

    public SMSService(String phoneNumber, String eventType, String userId, final MainWindow mainWindow) {
        this.userPhoneNumber = phoneNumber;
        this.eventType = eventType;
        this.mainWindow = mainWindow;
        this.textEvents = mainWindow.getTextEvents();
        this.userId = userId;
    }

    public SMSService(String phoneNumber, JSONObject jsonValue, String eventType, final MainWindow mainWindow) {
        this.userPhoneNumber = phoneNumber;
        this.jsonValue = jsonValue;
        this.mainWindow = mainWindow;
        this.eventType = eventType;
        this.textEvents = mainWindow.getTextEvents();
    }

    public static final String ACCOUNT_SID = "";
    public static final String AUTH_TOKEN = "";


    @Override
    public void run() {

        String filename = null;
        String eventMessage = "";
        File file;
        Scanner scanner;

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


        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

       Message message = Message
                .creator(new PhoneNumber(userPhoneNumber), new PhoneNumber("+16016754248"),
                        eventMessage)
                .create();

        System.out.println(message.getSid());
        textEvents.append("SMS send to = " + userId + " about event = " + eventType + "\n ==================================== \n");
        textEvents.setCaretPosition(textEvents.getText().length() - 1);
    }
}
