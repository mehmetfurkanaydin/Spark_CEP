package com.mfa.spark;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Created by mfa on 17.01.2017.
 */
public class EventConsumer implements Runnable {


    private  KafkaConsumer<String, String> consumer;
    private MainWindow mainWindow;
    private JTextArea textEvents;

    public EventConsumer(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.textEvents = mainWindow.getTextEvents();
    }

    @Override
    public void run() {
        ArrayList topics = new ArrayList();
        topics.add("events");
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
        System.out.println("Subscribed to topic event");

        TransportClient client = null;
        try {
            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        JSONParser parser = new JSONParser();
        JSONObject json = null;


        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                //  System.out.printf("offset = %d, key = %s, value = %s\n",
                //          record.offset(), record.key(), record.value());
                System.out.println(record.value());
                try {
                    json = (JSONObject) parser.parse(record.value());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                IndexResponse response = client.prepareIndex("event", "event")
                        .setSource(json)
                        .get();

            }
        }

    }

}

