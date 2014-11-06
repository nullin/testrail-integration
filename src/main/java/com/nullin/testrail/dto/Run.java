package com.nullin.testrail.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Test run
 *
 * @author nullin
 */
public class Run {

    public int id;
    @JsonProperty("project_id")
    public Integer projectId;
    @JsonProperty("suite_id")
    public int suiteId;
    @JsonProperty("plan_id")
    public Integer planId;
    public String name;
    @JsonProperty("config_ids")
    public List<Integer> configIds;

}
