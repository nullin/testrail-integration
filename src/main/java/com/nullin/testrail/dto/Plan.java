package com.nullin.testrail.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Test Plan
 *
 * @author nullin
 */
public class Plan {

    public int id;
    public String name;
    @JsonProperty("milestone_id")
    public Integer milestoneId;
    public List<PlanEntry> entries;

}
