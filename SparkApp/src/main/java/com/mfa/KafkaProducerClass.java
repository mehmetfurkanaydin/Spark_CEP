package com.mfa;

/**
 * Created by mfa on 16.01.2017.
 */


import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

//Create java class named “SimpleProducer”
public  class KafkaProducerClass {


    public  void sendMessage(String topic, String message){

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());

        KafkaProducer<String,String> producer = new KafkaProducer<String,String>(props);

        boolean sync = false;
        String key = "alert";
        String value = message;
        ProducerRecord<String,String> producerRecord = new ProducerRecord<String,String>(topic, key, value);
        if (sync) {
            try {
                producer.send(producerRecord).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            producer.send(producerRecord);
        }
        producer.close();
    }
}