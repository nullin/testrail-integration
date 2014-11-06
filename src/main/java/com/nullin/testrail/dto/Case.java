package com.nullin.testrail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Case represents a single test case in TestRail
 *
 * @author nullin
 */
public class Case {

    public int id;
    public String title;
    @JsonProperty("suite_id")
    public int suiteId;
    @JsonProperty("type_id")
    public int typeId;
    @JsonProperty("milestone_id")
    public int milestoneId;
    @JsonProperty("section_id")
    public int sectionId;
    //following property is not present in TestRail by default
    //we need to add it as a custom field
    @JsonProperty("custom_automation_id")
    public String automationId;


}
