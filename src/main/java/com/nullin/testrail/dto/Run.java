package com.nullin.testrail.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TODO: add javadocs!
 *
 * @author nullin
 */
public class Run {

    public int id;
    @JsonProperty("suite_id")
    public int suiteId;
    @JsonProperty("plan_id")
    public Integer planId;
    public String name;
    @JsonProperty("config_ids")
    public List<Integer> configIds;

}
