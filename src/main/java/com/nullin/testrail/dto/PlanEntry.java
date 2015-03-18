package com.nullin.testrail.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Test Plan entry
 *
 * @author nullin
 */
public class PlanEntry {

    public String id;
    @JsonProperty("suite_id")
    public int suiteId;
    public String name;
    public List<Run> runs;
    @JsonProperty("assignedto_id")
    public Integer assignedTo;
    @JsonProperty("include_all")
    public boolean includeAll;
    @JsonProperty("case_ids")
    public List<Integer> caseIds;
    @JsonProperty("config_ids")
    public List<Integer> configIds;

}
