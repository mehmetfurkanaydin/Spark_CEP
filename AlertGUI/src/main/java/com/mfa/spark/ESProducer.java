package com.mfa.spark;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * Created by mfa on 23.01.2017.
 */
public class ESProducer {

    public static void writeToES(String jsonString, String index, String type, TransportClient client) {
        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) parser.parse(jsonString);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        IndexResponse response = client.prepareIndex(index, type)
                .setSource(json)
                .get();

    }
}
