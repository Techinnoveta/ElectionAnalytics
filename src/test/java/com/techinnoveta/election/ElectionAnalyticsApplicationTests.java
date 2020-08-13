package com.techinnoveta.election;

import com.techinnoveta.election.controller.service.MainService;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class ElectionAnalyticsApplicationTests {

	@Autowired
	private MainService mainService;

	@Test
	void contextLoads() {
	}

	@Test
	void loadAllDistrict() throws Exception {
		mainService.loadAllDistrict("2019");
	}


}
