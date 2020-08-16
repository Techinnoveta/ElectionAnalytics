package com.techinnoveta.election.controller.repo;

/*
 * @created 11/08/2020 - 11:25 PM
 * @author thanushankanagarajah
 * @use - TODO
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class MainRepo {
    @Autowired
    private HikariCPDataSource hikariCPDataSource;


    private static Logger log = LogManager.getLogger(MainRepo.class);

    Connection beginTransaction() {

        log.info("MainRepo: start beginTransaction ");

        Connection con = hikariCPDataSource.getConnection();

        try {
            con.setAutoCommit(false);
        } catch (Exception ex) {
            log.error(ex);
        }

        log.info("MainRepo: end beginTransaction : connection status : {} ", (con == null));

        return con;
    }

    void commitTransaction(Connection con) {

        log.info("MainRepo: start commitTransaction ");

        try {
            con.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex);
        }

        log.info("MainRepo: end commitTransaction : connection status : {} ", (con == null));

    }

    void rollbackTransaction(Connection con) {

        log.info("MainRepo: start rollbackTransaction ");

        try {

            if (con != null) {
                con.rollback();
                con.setAutoCommit(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex);
        }

        log.info("MainRepo: end rollbackTransaction : connection status : {} ", (con == null));

    }

    public long executeUpdate(Connection con, String sql) throws Exception {

        log.info("MainRepo: executeUpdate : {}", sql);

        PreparedStatement pst = null;
        ResultSet rs = null;
        long id = -1;

        try {

            con = (con == null) ? hikariCPDataSource.getConnection() : con;

            if (con == null)
                throw new SQLException("CONNECTION NULL");

            pst = con.prepareStatement(sql);

            pst.execute();

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new SQLException(ex.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception ex1) {
                log.error(ex1.getMessage(), ex1);
            }
            try {
                if (pst != null) pst.close();
            } catch (Exception ex2) {
                log.error(ex2.getMessage(), ex2);
            }
        }

        return id; //con will be reused and intentionally not closed
    }

    public JSONArray getDataPaging(String sql) throws Exception {

        log.info("MainRepo: getDataPaging : started : {}", sql);

        StringBuilder sb = new StringBuilder();
        JSONArray ret;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {

            Connection con = hikariCPDataSource.getConnection();

            if (con == null)
                throw new SQLException("UNABLE_TO_RETRIEVE_DATA");

            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();
            ret = buildJSONObject(rs);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (Exception ex1) {
                log.error(ex1.getMessage(), ex1);
            }
            try {
                if (pst != null) pst.close();
            } catch (Exception ex2) {
                log.error(ex2.getMessage(), ex2);
            }
        }

        return ret;
    }

    private JSONArray buildJSONObject(ResultSet rs) {

        JSONArray json = new JSONArray();

        ResultSetMetaData rsmd;

        try {
            rsmd = rs.getMetaData();
            int numColumns = rsmd.getColumnCount();

            while (rs.next()) {
                JSONObject obj = new JSONObject();

                for (int i = 1; i <= numColumns; i++) {

                    String column_name = rsmd.getColumnName(i);
                    Object value = rs.getObject(column_name);
                    // Convert null values to JSON null type
                    obj.put(column_name, value == null ? null : value);
                }
                json.add(obj);
            }
        } catch (Exception ex) {
            log.error("BaseDataServiceImpl:buildJSONObject:", ex);
        }

        return json;
    }
}
