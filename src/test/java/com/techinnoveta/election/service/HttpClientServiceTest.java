package com.techinnoveta.election.service;

/*
 * @created 12/08/2020 - 7:51 AM
 * @author thanushankanagarajah
 * @use - TODO
 */

import com.techinnoveta.election.controller.service.HttpClientService;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class HttpClientServiceTest {
    @Autowired
    private HttpClientService httpClientService;

    @Test
    public void callGet() throws Exception {

        HttpGet request = new HttpGet("https://election.newsfirst.lk/result/new-result-district?district_id=10&type=all");

        // add request headers
        request.addHeader("accept", "application/json, text/javascript, */*; q=0.01");
        request.addHeader("content-type", "application/json");
        request.addHeader("Cookie", "XSRF-TOKEN=eyJpdiI6InJscnU1MVMrSUdObUZzUysybFk4c2c9PSIsInZhbHVlIjoiUDhxNnk0eisxMWNzdmRyZW5IQkUvTkVQNUlWY0xQVnF2SDMvQnFxSU52QnhBT2llK3JtOTJ4MFgxSzkvaks5dyIsIm1hYyI6ImFlYzM3OWQ4OGEwMjBmNzZlYTA1ZWQyZmNlNTc0OWM2ZTBlMjVlNTg3NWE0Y2JhODY1MjgyOWNkNWRmMDkyNDEifQ%3D%3D; laravel_session=eyJpdiI6IlU1dlNxV2VLOW03bHVTUmZqUURuUWc9PSIsInZhbHVlIjoiVjdSMXdiOW9HcXNwQlpCZlFVcFlBOEZCVG1vNmJXRkFXb3poZU93dlBqTGFYVkZMb05ZaXJVRi9kcStFd0wwbyIsIm1hYyI6ImYxYTM4ODkxNWJkYjEyNjYwNjU1NDhlYzIzOGQ0Mjc2NjU1YWQyMDRhMjQ5MDZlMDQxMjg1ODZmNzU2NTk1MTYifQ%3D%3D");

        httpClientService.sendGetObject(request);

    }

    @Test
    private void callPost() throws Exception {
        HttpPost post = new HttpPost("https://election.newsfirst.lk/result/new-result-district?district_id=10&type=all");

        // add request parameter, form parameters
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("username", "abc"));
        urlParameters.add(new BasicNameValuePair("password", "123"));
        urlParameters.add(new BasicNameValuePair("custom", "secret"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        httpClientService.sendPost(post);

    }
}
