package com.mfa.spark;

import javax.swing.*;


import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import java.util.*;


/**
 * Created by mfa on 17.01.2017.
 */
public class Consumer implements Runnable {


    private KafkaConsumer<String, String> consumer;
    private MainWindow mainWindow;
    private JTextArea textEvents;
    private JTextArea textRules;
    private JTextArea textCF;

    public Consumer(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.textEvents = mainWindow.getTextEvents();
        this.textRules = mainWindow.getTextRules();
        this.textCF = mainWindow.getTextCF();
    }

    @Override
    public void run() {
        ArrayList topics = new ArrayList();
        topics.add("cfAlerts");
        topics.add("ruleAlerts");
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("enable.auto.commit", "true");
        props.put("group.id", "1");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

        consumer.subscribe(topics);
        System.out.println("Subscribed to topic cf alerts and rule alerts ");
        int i = 0;


        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                //  System.out.printf("offset = %d, key = %s, value = %s\n",
                //          record.offset(), record.key(), record.value());

                switch (record.topic()) {
                    case "cfAlerts":
                        Thread cfThread = new Thread(new IpEs(record.value(), this.mainWindow));
                        cfThread.start();
                        textCF.append(record.value() + "\n ==================================== \n");
                        textCF.setCaretPosition(textCF.getText().length() - 1);
                        break;
                    case "ruleAlerts":
                        Thread userThread = new Thread(new UserES(record.value(), this.mainWindow));
                        userThread.start();
                        textRules.append(record.value() + "\n ==================================== \n");
                        textRules.setCaretPosition(textRules.getText().length() - 1);
                        break;
                }


            }
        }

    }
}
