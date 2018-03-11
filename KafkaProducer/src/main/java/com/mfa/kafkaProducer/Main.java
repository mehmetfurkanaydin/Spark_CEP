package com.mfa.kafkaProducer;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.Scanner;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;


public class Main {
    public static void main(String[] args) {
        String filename = "/tmpSecondsSimulator.txt";
        File file = new File(filename);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String topic = "eventLogs";
        String brokers = "localhost:9092";


        Properties props = new Properties();
        props.put("metadata.broker.list", brokers);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        //props.put("partitioner.class", "com.colobu.kafka.SimplePartitioner");
        props.put("producer.type", "async");
        //props.put("request.required.acks", "1");

        ProducerConfig config = new ProducerConfig(props);

        Producer<String, String> producer = new Producer<String, String>(config);

        while(scanner.hasNextLine()){
                String msg = scanner.nextLine();
                KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic, msg);
                producer.send(data);
        }


        producer.close();
    }
}