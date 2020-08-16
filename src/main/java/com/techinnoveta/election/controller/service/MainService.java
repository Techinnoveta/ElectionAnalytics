package com.techinnoveta.election.controller.service;

/*
 * @created 11/08/2020 - 11:01 PM
 * @author thanushankanagarajah
 * @use - TODO
 */

import com.techinnoveta.election.controller.modal.ElectoralVote;
import com.techinnoveta.election.controller.repo.HikariCPDataSource;
import com.techinnoveta.election.controller.repo.MainRepo;
import com.techinnoveta.election.util.CONSTANTS;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.Iterator;

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

    @Value("${election.result.endpoint}")
    private String endPoint;

    public void loadAllDistrict(String year) throws Exception {
        HttpGet request = new HttpGet(endPoint + "." + year + ".json");

        JSONArray jsonArray = httpClientService.sendGetArray(request);

        Connection con = hikariCPDataSource.getConnection();

        Iterator<JSONObject> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JSONObject obj = iterator.next();
            if (obj.get("type").equals(CONSTANTS.POLLING_VOTE)) {
                String sql = "INSERT INTO electoral_districts(ed_code, name) VALUES (\'" + obj.get("ed_code") + "\',\'" + obj.get("ed_name") + "\') " +
                        "ON CONFLICT DO NOTHING";
                mainRepo.executeUpdate(con, sql);
            }

        }

        // add National Record
        String sql = "INSERT INTO electoral_districts(ed_code, name) VALUES ('" + CONSTANTS.NATIONAL_CODE + "', '" + "NATIONAL" + "') " +
                "ON CONFLICT DO NOTHING";
        mainRepo.executeUpdate(con, sql);
    }

    public void loadAllElectorate(String year) throws Exception {
        HttpGet request = new HttpGet(endPoint + "." + year + ".json");

        JSONArray jsonArray = httpClientService.sendGetArray(request);

        Connection con = hikariCPDataSource.getConnection();

        Iterator<JSONObject> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JSONObject obj = iterator.next();
            if (obj.get("type").equals(CONSTANTS.POLLING_VOTE)) {
                String sql = "INSERT INTO public.polling_divisions(pd_code, name, ed_code) VALUES " +
                        "(\'" + obj.get("pd_code") + "\',\'" + obj.get("pd_name") + "\',\'" + obj.get("ed_code") + "\') " +
                        "ON CONFLICT DO NOTHING";
                mainRepo.executeUpdate(con, sql);
            }
        }
    }

    public void loadAllParties(String year) throws Exception {
        HttpGet request = new HttpGet(endPoint + "." + year + ".json");

        JSONArray jsonArray = httpClientService.sendGetArray(request);

        Connection con = hikariCPDataSource.getConnection();

        Iterator<JSONObject> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JSONObject obj = iterator.next();
            if (obj.get("type").equals(CONSTANTS.POLLING_VOTE)) {
                JSONArray partiesArray = (JSONArray) obj.get("by_party");
                Iterator<JSONObject> iteratorParties = partiesArray.iterator();
                while (iteratorParties.hasNext()) {
                    JSONObject jsonObjParty = iteratorParties.next();
                    String sql = "INSERT INTO public.parties_data(party_code, party_name) VALUES " +
                            "(\'" + jsonObjParty.get("party_code") + "\',\'" + jsonObjParty.get("party_name").toString().replace("\'", "\''") + "\') " +
                            "ON CONFLICT DO NOTHING";
                    mainRepo.executeUpdate(con, sql);
                }
            }
        }
    }

    public void loadAllVotesData(String year) throws Exception {
        HttpGet request = new HttpGet(endPoint + "." + year + ".json");

        ElectoralVote electoralVote = new ElectoralVote();

        JSONArray jsonArray = httpClientService.sendGetArray(request);

        Connection con = hikariCPDataSource.getConnection();

        Iterator<JSONObject> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JSONObject obj = iterator.next();
            if (obj.get("type").equals(CONSTANTS.POLLING_VOTE)) {

                electoralVote.setYear(year);
                electoralVote.setType(obj.get("type").toString());
                electoralVote.setEd_code(obj.get("ed_code").toString());
                electoralVote.setPd_code(obj.get("pd_code").toString());

                JSONArray partiesArray = (JSONArray) obj.get("by_party");
                Iterator<JSONObject> iteratorParties = partiesArray.iterator();

                JSONObject summaryObj = (JSONObject) obj.get("summary");
                electoralVote.setElectors(convertInteger(summaryObj.get("electors").toString()));
                electoralVote.setPolled(convertInteger(summaryObj.get("polled").toString()));
                electoralVote.setValid_vote(convertInteger(summaryObj.get("valid").toString()));
                electoralVote.setRejected(convertInteger(summaryObj.get("rejected").toString()));
                electoralVote.setPercent_polled(convertDouble(summaryObj.get("percent_polled").toString().replace("%", "")));
                electoralVote.setPercent_valid(convertDouble(summaryObj.get("percent_valid").toString().replace("%", "")));
                electoralVote.setPercent_rejected(convertDouble(summaryObj.get("percent_rejected").toString().replace("%", "")));

                while (iteratorParties.hasNext()) {
                    JSONObject jsonObjParty = iteratorParties.next();
                    electoralVote.setParty_code(jsonObjParty.get("party_code").toString());
                    electoralVote.setVote_count(convertInteger(jsonObjParty.get("vote_count").toString()));
                    electoralVote.setVote_percentage(convertDouble(jsonObjParty.get("vote_percentage").toString().replace("%", "")));

                    String sql = "INSERT INTO vote_data(type, year, vote_count, vote_percentage, electors, polled, rejected, percent_polled, " +
                            "percent_valid, percent_rejected, pd_code, ed_code, party_code, valid_vote) " +
                            "VALUES ('" + electoralVote.getType() + "', " +
                            "'" + electoralVote.getYear() + "', " +
                            "'" + electoralVote.getVote_count() + "', " +
                            "'" + electoralVote.getVote_percentage() + "', " +
                            "'" + electoralVote.getElectors() + "', " +
                            "'" + electoralVote.getPolled() + "', " +
                            "'" + electoralVote.getRejected() + "', " +
                            "'" + electoralVote.getPercent_polled() + "', " +
                            "'" + electoralVote.getPercent_valid() + "', " +
                            "'" + electoralVote.getPercent_rejected() + "', " +
                            "'" + electoralVote.getPd_code() + "', " +
                            "'" + electoralVote.getEd_code() + "', " +
                            "'" + electoralVote.getParty_code() + "', " +
                            "'" + electoralVote.getValid_vote() + "') " +
                            "ON CONFLICT (type, year, pd_code, ed_code, party_code) DO NOTHING";
                    mainRepo.executeUpdate(con, sql);
                }

            }
        }
    }

    public void loadAllSeatCountData(String year) throws Exception {
        HttpGet request = new HttpGet(endPoint + "." + year + ".json");

        JSONArray jsonArray = httpClientService.sendGetArray(request);

        Connection con = hikariCPDataSource.getConnection();

        Iterator<JSONObject> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JSONObject obj = iterator.next();

            // Load distict_list_seat_count
            if (obj.get("type").equals(CONSTANTS.POLLING_ELECTED_SEAT)) {
                JSONArray partiesArray = (JSONArray) obj.get("by_party");
                String ed_code = obj.get("ed_code").toString();
                Iterator<JSONObject> iteratorParties = partiesArray.iterator();
                while (iteratorParties.hasNext()) {
                    JSONObject jsonObjParty = iteratorParties.next();
                    if (null != jsonObjParty.get("seat_count") && Integer.parseInt(jsonObjParty.get("seat_count").toString()) > 0) {

                        String sql = "INSERT INTO public.seat_data(party_code, ed_code, seat_count, seat_type, year) " +
                                "VALUES ('" + jsonObjParty.get("party_code") + "', '" + ed_code + "', " +
                                "'" + jsonObjParty.get("seat_count") + "', '" + CONSTANTS.ELECTED + "', '" + year + "') " +
                                "ON CONFLICT (party_code, ed_code, seat_type, year) DO UPDATE SET seat_count = '" + jsonObjParty.get("seat_count") + "'";
                        mainRepo.executeUpdate(con, sql);
                    }
                }
            }

            // Load national_list_seat_count
            if (obj.get("type").equals(CONSTANTS.POLLING_NATIONAL_VOTE)) {
                JSONArray partiesArray = (JSONArray) obj.get("by_party");
                Iterator<JSONObject> iteratorParties = partiesArray.iterator();
                while (iteratorParties.hasNext()) {
                    JSONObject jsonObjParty = iteratorParties.next();
                    if (null != jsonObjParty.get("national_list_seat_count") && Integer.parseInt(jsonObjParty.get("national_list_seat_count").toString()) > 0) {

                        String sql = "INSERT INTO public.seat_data(party_code, ed_code, seat_count, seat_type, year) " +
                                "VALUES ('" + jsonObjParty.get("party_code") + "', '" + CONSTANTS.NATIONAL_CODE + "', " +
                                "'" + jsonObjParty.get("national_list_seat_count") + "', '" + CONSTANTS.ELECTED + "', '" + year + "') " +
                                "ON CONFLICT (party_code, ed_code, seat_type, year) DO UPDATE SET seat_count = '" + jsonObjParty.get("seat_count") + "'";
                        mainRepo.executeUpdate(con, sql);
                    }
                }
            }
        }
    }

    public void loadAllElectedCandidateList(String year) throws Exception {
        HttpGet request = new HttpGet(endPoint + "." + year + ".json");

        JSONArray jsonArray = httpClientService.sendGetArray(request);

        Connection con = hikariCPDataSource.getConnection();

        Iterator<JSONObject> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JSONObject obj = iterator.next();

            if (obj.get("type").equals(CONSTANTS.POLLING_ELECTED_CANDIDATE)) {
                JSONArray partiesArray = (JSONArray) obj.get("by_candidate");
                String ed_code = obj.get("ed_code").toString();
                Iterator<JSONObject> iteratorParties = partiesArray.iterator();
                while (iteratorParties.hasNext()) {
                    JSONObject jsonObjParty = iteratorParties.next();
                    if (null != partiesArray) {

                        String sql = "INSERT INTO public.elected_candidate(party_code, ed_code, candidate_name, year) " +
                                "VALUES ('" + jsonObjParty.get("party_code") + "', '" + ed_code + "', " +
                                "'" + jsonObjParty.get("candidate_name") + "', '" + year + "') " +
                                "ON CONFLICT DO NOTHING";
                        mainRepo.executeUpdate(con, sql);
                    }
                }
            }
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

    private int convertInteger(String value) {
        int val = 0;
        try {
            val = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            val = 0;
        }
        return val;
    }

    private double convertDouble(String value) {
        double val = 0;
        try {
            val = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            val = 0;
        }
        return val;
    }
}
