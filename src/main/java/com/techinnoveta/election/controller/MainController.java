package com.techinnoveta.election.controller;

import com.techinnoveta.election.controller.service.MainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/*
 * @created 11/08/2020 - 10:57 PM
 * @author thanushankanagarajah
 * @use - TODO
 */
@Controller
@Slf4j
@EnableAutoConfiguration
@CrossOrigin
@Scope("prototype")
@ComponentScan("com.techinnoveta.election")
public class MainController {
    @Autowired
    private MainService mainService;

    @PostMapping(value="/load/district")
    @ResponseBody
    public String loadAllDistrict() throws Exception {
        mainService.loadAllDistrict();
        return "Success";
    }


    @PostMapping(value="/load/electorate")
    @ResponseBody
    public String loadAllElectorate() throws Exception {
        mainService.loadAllElectorate();
        return "Success";
    }

    @PostMapping(value="/load/parties")
    @ResponseBody
    public String loadAllParties() throws Exception {
        mainService.loadAllParties();
        return "Success";
    }

}
