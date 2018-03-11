package com.mfa.spark;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.reindex.BulkIndexByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by mfa on 24.01.2017.
 */
public class DeleteAlertIndexes {

    public static void  deleteAlertIndexes() {

        TransportClient client = null;
        try {
            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        BulkIndexByScrollResponse response =
                DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                        .source("alerts")
                        .get();

        response.getDeleted();

    }
}
