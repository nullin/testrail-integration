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

}
