package com.nullin.testrail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TODO: add javadocs!
 *
 * @author nullin
 */
public class Case {

    public int id;
    @JsonProperty("suite_id")
    public int suiteId;
    public String title;

}
