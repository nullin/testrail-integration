package com.nullin.testrail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a test instance (an instance of a test case)
 *
 * @author nullin
 */
public class Test {

    public int id;
    @JsonProperty("case_id")
    public int caseId;
    @JsonProperty("statusId")
    public int status_id;
    @JsonProperty("run_id")
    public int runId;

}
