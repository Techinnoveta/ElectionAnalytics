package com.techinnoveta.election.controller.repo;

/*
 * @created 11/08/2020 - 11:30 PM
 * @author thanushankanagarajah
 * @use - TODO
 */

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Component
public final class HikariCPDataSource {

    private static Logger log = LogManager.getLogger(HikariCPDataSource.class);

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.driver-class-name}")
    private String driver;

    private HikariConfig config = new HikariConfig();
    private HikariDataSource ds;
    private Connection con;

    public final Connection getConnection() {

        try {

            if (con == null) {

                config.setJdbcUrl(url);
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName(driver);
                ds = new HikariDataSource(config);
                con = ds.getConnection();
            }

            return con;

        } catch (Exception ex) {
            log.error("FATAL : Error creating DB connection", ex);
        }
        return null;
    }

    public HikariCPDataSource() {


    }


}