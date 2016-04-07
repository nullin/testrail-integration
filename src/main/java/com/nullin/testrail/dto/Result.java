package com.nullin.testrail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Test Result
 *
 * @author nullin
 */
public class Result {

    public int id;
    @JsonProperty("test_id")
    public int testId;
    @JsonProperty("status_id")
    public Integer statusId;
    public String comment;

}
