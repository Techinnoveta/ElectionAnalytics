package com.techinnoveta.election.controller.service;

/*
 * @created 11/08/2020 - 11:01 PM
 * @author thanushankanagarajah
 * @use - TODO
 */

import com.techinnoveta.election.controller.repo.HikariCPDataSource;
import com.techinnoveta.election.controller.repo.MainRepo;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.sql.Connection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

@Component
@ComponentScan("com.techinnoveta.election")
@PropertySource("classpath:application.properties")
public class MainService {

    @Autowired
    private MainRepo mainRepo;

    @Autowired
    private HikariCPDataSource hikariCPDataSource;

    @Autowired
    private HttpClientService httpClientService;

    public void loadAllDistrict() throws Exception {
        JSONParser parser = new JSONParser();
        Object object = parser.parse(new FileReader(getFileFromResources()));

        JSONObject jsonObject = (JSONObject) object;

        HttpGet request = new HttpGet("https://election.newsfirst.lk/result/all-country-top?_=1597191020777");

        // add request headers
        request.addHeader("accept", "application/json, text/javascript, */*; q=0.01");
        request.addHeader("content-type", "application/json");
        request.addHeader("Cookie", "XSRF-TOKEN=eyJpdiI6InJscnU1MVMrSUdObUZzUysybFk4c2c9PSIsInZhbHVlIjoiUDhxNnk0eisxMWNzdmRyZW5IQkUvTkVQNUlWY0xQVnF2SDMvQnFxSU52QnhBT2llK3JtOTJ4MFgxSzkvaks5dyIsIm1hYyI6ImFlYzM3OWQ4OGEwMjBmNzZlYTA1ZWQyZmNlNTc0OWM2ZTBlMjVlNTg3NWE0Y2JhODY1MjgyOWNkNWRmMDkyNDEifQ%3D%3D; laravel_session=eyJpdiI6IlU1dlNxV2VLOW03bHVTUmZqUURuUWc9PSIsInZhbHVlIjoiVjdSMXdiOW9HcXNwQlpCZlFVcFlBOEZCVG1vNmJXRkFXb3poZU93dlBqTGFYVkZMb05ZaXJVRi9kcStFd0wwbyIsIm1hYyI6ImYxYTM4ODkxNWJkYjEyNjYwNjU1NDhlYzIzOGQ0Mjc2NjU1YWQyMDRhMjQ5MDZlMDQxMjg1ODZmNzU2NTk1MTYifQ%3D%3D");

        jsonObject = httpClientService.sendGet(request);

        JSONArray allDivision = (JSONArray) jsonObject.get("districtResult");

        Connection con = hikariCPDataSource.getConnection();

        Iterator<JSONObject> iterator = allDivision.iterator();
        while (iterator.hasNext()) {
            JSONObject obj = iterator.next();
            String sql = "INSERT INTO district (name, district_id) VALUES (\'"+ obj.get("name") +"\',\'"+ obj.get("id") +"\') " +
                    "ON CONFLICT DO NOTHING";
            mainRepo.executeUpdate(con,sql);
        }
    }

    public void loadAllElectorate() throws Exception {
        JSONParser parser = new JSONParser();
        Object object = parser.parse(new FileReader(getFileFromResources()));

        AtomicReference<JSONObject> jsonObject = new AtomicReference<>((JSONObject) object);

        String sql = "SELECT * FROM district ORDER BY district_id";
        JSONArray jsonArray = mainRepo.getDataPaging(sql);
        jsonArray.stream().forEach( json -> {
            jsonObject.set((JSONObject) json);
            try {
                loadElectorateByDistrictId(jsonObject.get().get("district_id").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void loadElectorateByDistrictId(String district_id) throws Exception {
        JSONParser parser = new JSONParser();
        Object object = parser.parse(new FileReader(getFileFromResources()));

        JSONObject jsonObject = (JSONObject) object;

        HttpGet request = new HttpGet("https://election.newsfirst.lk/result/new-result-district?district_id="+ district_id +"&type=all");

        // add request headers
        request.addHeader("accept", "application/json, text/javascript, */*; q=0.01");
        request.addHeader("content-type", "application/json");
        request.addHeader("Cookie", "XSRF-TOKEN=eyJpdiI6InJscnU1MVMrSUdObUZzUysybFk4c2c9PSIsInZhbHVlIjoiUDhxNnk0eisxMWNzdmRyZW5IQkUvTkVQNUlWY0xQVnF2SDMvQnFxSU52QnhBT2llK3JtOTJ4MFgxSzkvaks5dyIsIm1hYyI6ImFlYzM3OWQ4OGEwMjBmNzZlYTA1ZWQyZmNlNTc0OWM2ZTBlMjVlNTg3NWE0Y2JhODY1MjgyOWNkNWRmMDkyNDEifQ%3D%3D; laravel_session=eyJpdiI6IlU1dlNxV2VLOW03bHVTUmZqUURuUWc9PSIsInZhbHVlIjoiVjdSMXdiOW9HcXNwQlpCZlFVcFlBOEZCVG1vNmJXRkFXb3poZU93dlBqTGFYVkZMb05ZaXJVRi9kcStFd0wwbyIsIm1hYyI6ImYxYTM4ODkxNWJkYjEyNjYwNjU1NDhlYzIzOGQ0Mjc2NjU1YWQyMDRhMjQ5MDZlMDQxMjg1ODZmNzU2NTk1MTYifQ%3D%3D");

        jsonObject = httpClientService.sendGet(request);

        JSONArray allDivision = (JSONArray) jsonObject.get("allDivision");

        Connection con = hikariCPDataSource.getConnection();

        Iterator<JSONObject> iterator = allDivision.iterator();
        while (iterator.hasNext()) {
            JSONObject obj = iterator.next();
            String sql = "INSERT INTO polling_division (electorate_id, name, district_id, div_id) VALUES " +
                    "(\'"+ obj.get("id") +"\',\'"+ obj.get("name") +"\',\'"+ obj.get("district_id") +"\', \'"+ obj.get("div_id") +"\') " +
                    "ON CONFLICT DO NOTHING";
            mainRepo.executeUpdate(con,sql);
        }
    }

    public void loadAllParties() throws Exception {
        JSONParser parser = new JSONParser();
        Object object = parser.parse(new FileReader(getFileFromResources()));

        AtomicReference<JSONObject> jsonObject = new AtomicReference<>((JSONObject) object);

        String sql = "SELECT * FROM district ORDER BY district_id";
        JSONArray jsonArray = mainRepo.getDataPaging(sql);
        jsonArray.stream().forEach( json -> {
            jsonObject.set((JSONObject) json);
            try {
                loadParties(jsonObject.get().get("district_id").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void loadParties(String district_id) throws Exception {
        JSONParser parser = new JSONParser();
        Object object = parser.parse(new FileReader(getFileFromResources()));

        JSONObject jsonObject = (JSONObject) object;

        HttpGet request = new HttpGet("https://election.newsfirst.lk/result/new-result-district?district_id="+ district_id +"&type=all");

        // add request headers
        request.addHeader("accept", "application/json, text/javascript, */*; q=0.01");
        request.addHeader("content-type", "application/json");
        request.addHeader("Cookie", "XSRF-TOKEN=eyJpdiI6InJscnU1MVMrSUdObUZzUysybFk4c2c9PSIsInZhbHVlIjoiUDhxNnk0eisxMWNzdmRyZW5IQkUvTkVQNUlWY0xQVnF2SDMvQnFxSU52QnhBT2llK3JtOTJ4MFgxSzkvaks5dyIsIm1hYyI6ImFlYzM3OWQ4OGEwMjBmNzZlYTA1ZWQyZmNlNTc0OWM2ZTBlMjVlNTg3NWE0Y2JhODY1MjgyOWNkNWRmMDkyNDEifQ%3D%3D; laravel_session=eyJpdiI6IlU1dlNxV2VLOW03bHVTUmZqUURuUWc9PSIsInZhbHVlIjoiVjdSMXdiOW9HcXNwQlpCZlFVcFlBOEZCVG1vNmJXRkFXb3poZU93dlBqTGFYVkZMb05ZaXJVRi9kcStFd0wwbyIsIm1hYyI6ImYxYTM4ODkxNWJkYjEyNjYwNjU1NDhlYzIzOGQ0Mjc2NjU1YWQyMDRhMjQ5MDZlMDQxMjg1ODZmNzU2NTk1MTYifQ%3D%3D");

        jsonObject = httpClientService.sendGet(request);

        JSONArray allDivision = (JSONArray) jsonObject.get("allDivision");

        Connection con = hikariCPDataSource.getConnection();

        Iterator<JSONObject> iterator = allDivision.iterator();
        while (iterator.hasNext()) {
            JSONObject obj = iterator.next();
            JSONArray allParties = (JSONArray) obj.get("parties");
            Iterator<JSONObject> objectIterator = allParties.iterator();
            while (objectIterator.hasNext()) {
                JSONObject parObject = objectIterator.next();
                JSONObject party = (JSONObject) parObject.get("party");
                String sql = "INSERT INTO parties( party_id, name, ref_code, logo, color, display_name, party_type)" +
                        "VALUES (\'"+ party.get("id") +"\', " +
                        "\'"+ party.get("name").toString().replace("\'", "\''") +"\', " +
                        "\'"+ party.get("ref_code") +"\', " +
                        "\'"+ party.get("logo") +"\', " +
                        "\'"+ party.get("color") +"\', " +
                        "\'"+ party.get("display_name").toString().replace("\'", "\''") +"\', " +
                        "\'"+ party.get("party_type") +"\') " +
                        "ON CONFLICT DO NOTHING";
                mainRepo.executeUpdate(con, sql);
            }
        }
    }

    public void loadAllVotesData() throws Exception {
        JSONParser parser = new JSONParser();
        Object object = parser.parse(new FileReader(getFileFromResources()));

        AtomicReference<JSONObject> jsonObject = new AtomicReference<>((JSONObject) object);

        String sql = "SELECT * FROM district ORDER BY district_id";
        JSONArray jsonArray = mainRepo.getDataPaging(sql);
        jsonArray.stream().forEach( json -> {
            jsonObject.set((JSONObject) json);
            try {
                loadElectorateByDistrictId(jsonObject.get().get("district_id").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void loadAllVotesDataByDistrictId(String district_id) throws Exception {
        JSONParser parser = new JSONParser();
        Object object = parser.parse(new FileReader(getFileFromResources()));

        JSONObject jsonObject = (JSONObject) object;

        HttpGet request = new HttpGet("https://election.newsfirst.lk/result/new-result-district?district_id="+ district_id +"&type=all");

        // add request headers
        request.addHeader("accept", "application/json, text/javascript, */*; q=0.01");
        request.addHeader("content-type", "application/json");
        request.addHeader("Cookie", "XSRF-TOKEN=eyJpdiI6InJscnU1MVMrSUdObUZzUysybFk4c2c9PSIsInZhbHVlIjoiUDhxNnk0eisxMWNzdmRyZW5IQkUvTkVQNUlWY0xQVnF2SDMvQnFxSU52QnhBT2llK3JtOTJ4MFgxSzkvaks5dyIsIm1hYyI6ImFlYzM3OWQ4OGEwMjBmNzZlYTA1ZWQyZmNlNTc0OWM2ZTBlMjVlNTg3NWE0Y2JhODY1MjgyOWNkNWRmMDkyNDEifQ%3D%3D; laravel_session=eyJpdiI6IlU1dlNxV2VLOW03bHVTUmZqUURuUWc9PSIsInZhbHVlIjoiVjdSMXdiOW9HcXNwQlpCZlFVcFlBOEZCVG1vNmJXRkFXb3poZU93dlBqTGFYVkZMb05ZaXJVRi9kcStFd0wwbyIsIm1hYyI6ImYxYTM4ODkxNWJkYjEyNjYwNjU1NDhlYzIzOGQ0Mjc2NjU1YWQyMDRhMjQ5MDZlMDQxMjg1ODZmNzU2NTk1MTYifQ%3D%3D");

        jsonObject = httpClientService.sendGet(request);

        JSONArray allDivision = (JSONArray) jsonObject.get("allDivision");

        Connection con = hikariCPDataSource.getConnection();

        Iterator<JSONObject> iterator = allDivision.iterator();
        while (iterator.hasNext()) {
            JSONObject obj = iterator.next();
            String sql = "INSERT INTO polling_division (electorate_id, name, district_id, div_id) VALUES " +
                    "(\'"+ obj.get("id") +"\',\'"+ obj.get("name") +"\',\'"+ obj.get("district_id") +"\', \'"+ obj.get("div_id") +"\') " +
                    "ON CONFLICT DO NOTHING";
            mainRepo.executeUpdate(con,sql);
        }
    }

    // get file from classpath, resources folder
    private File getFileFromResources() {

        ClassLoader classLoader = getClass().getClassLoader();

        URL resource = classLoader.getResource("LoadData.json");
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }

    }
}
