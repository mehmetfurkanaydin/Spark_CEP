package com.mfa.spark;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
/**
 * Created by mfa on 19.01.2017.
 */
public class IpEs implements Runnable {

    private MainWindow mainWindow;
    private TransportClient client;

    private String jsonString;
    public IpEs (String jsonString, final MainWindow mainWindow) {
        this.jsonString = jsonString;
        this.mainWindow = mainWindow;
    }

    @Override
    public void run() {

        TransportClient client = null;
        try {
            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ESProducer.writeToES(jsonString, "alerts", "cf", client);

        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) parser.parse(this.jsonString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SearchResponse response = client.prepareSearch("ip")
                .setTypes("ip")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.termQuery("ip", json.get("ip")))                 // Query r
                .setFrom(0).setSize(60).setExplain(true)
                .get();

        SearchHit[] results = response.getHits().getHits();
        for (SearchHit hit : results) {
            System.out.println(hit.getId());    //prints out the id of the document
            Map<String, Object> result = hit.getSource();   //the retrieved document
            Thread smsThread = new Thread(new SMSService(result.get("phone").toString(), json, "cf", this.mainWindow));
            smsThread.start();
            Thread emailThread = new Thread(new EmailService(result.get("email").toString(), json, "cf", this.mainWindow));
            emailThread.start();
        }

        client.close();
    }

}
