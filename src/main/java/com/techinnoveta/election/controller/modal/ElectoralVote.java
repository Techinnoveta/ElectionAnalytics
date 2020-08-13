package com.techinnoveta.election.controller.modal;

/*
 * @created 12/08/2020 - 11:09 PM
 * @author thanushankanagarajah
 * @use - TODO
 */


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ElectoralVote {
    private String type;
    private String year;
    private int vote_count;
    private Double vote_percentage;
    private int electors;
    private int polled;
    private int rejected;
    private int valid_vote;
    private Double percent_polled;
    private Double percent_valid;
    private Double percent_rejected;
    private String pd_code;
    private String ed_code;
    private String party_code;
}
