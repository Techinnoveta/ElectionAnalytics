package com.techinnoveta.election;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:application.properties")
@SpringBootApplication
public class ElectionAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectionAnalyticsApplication.class, args);
    }

}
