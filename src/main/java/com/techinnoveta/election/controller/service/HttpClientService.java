package com.techinnoveta.election.controller.service;

/*
 * @created 12/08/2020 - 7:32 AM
 * @author thanushankanagarajah
 * @use - TODO
 */

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HttpClientService {

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private void close() throws IOException {
        httpClient.close();
    }

    public JSONObject sendGet(HttpGet request) throws Exception {

        JSONObject jsonObject = null;

        try (CloseableHttpResponse response = httpClient.execute(request)) {

            // Get HttpResponse Status
            System.out.println(response.getStatusLine().toString());

            HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();
            System.out.println(headers);

            if (entity != null) {
                JSONParser parser = new JSONParser();
                jsonObject = (JSONObject) parser.parse(EntityUtils.toString(entity));
            }
        }
        return jsonObject;
    }

    public JSONObject sendPost(HttpPost post) throws Exception {

        JSONObject jsonObject = null;

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            JSONParser parser = new JSONParser();
            jsonObject = (JSONObject) parser.parse(EntityUtils.toString(response.getEntity()));
        }
        return jsonObject;
    }

}
