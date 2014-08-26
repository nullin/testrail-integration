package com.nullin.testrail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TODO: add javadocs!
 *
 * @author nullin
 */
public class Result {

    public int id;
    @JsonProperty("test_id")
    public int testId;
    @JsonProperty("status_id")
    public int statusId;
    public String comment;

}
