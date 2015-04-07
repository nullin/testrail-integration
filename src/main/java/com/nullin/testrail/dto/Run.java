package com.nullin.testrail.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Test run
 *
 * @author nullin
 */
public class Run {

    public Integer id;
    @JsonProperty("project_id")
    public Integer projectId;
    @JsonProperty("suite_id")
    public Integer suiteId;
    @JsonProperty("plan_id")
    public Integer planId;
    public String name;
    @JsonProperty("assignedto_id")
    public Integer assignedTo;
    @JsonProperty("include_all")
    public boolean includeAll;
    @JsonProperty("case_ids")
    public List<Integer> caseIds;
    @JsonProperty("config_ids")
    public List<Integer> configIds;
    public String config;

}
