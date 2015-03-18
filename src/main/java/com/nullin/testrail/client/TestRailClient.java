package com.nullin.testrail.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nullin.testrail.dto.Case;
import com.nullin.testrail.dto.Plan;
import com.nullin.testrail.dto.PlanEntry;
import com.nullin.testrail.dto.Result;
import com.nullin.testrail.dto.Run;
import com.nullin.testrail.dto.Section;
import com.nullin.testrail.dto.Suite;
import com.nullin.testrail.dto.Test;

/**
 * TestRail Client for endpoints described at
 * {@link http://docs.gurock.com/testrail-api2/start}
 *
 * Contains methods to talk to all the various endpoints for all the
 * different object types within this one class. The method parameters
 * translate to the fields accepted as part of the request URL as well as a
 * map of fields that can be passed as body of the POST requests
 *
 * Client works with TestRails v4.0
 *
 * @author nullin
 */
public class TestRailClient {

    //underlying api client
    private APIClient client;
    //(de)-serializes objects to/from json
    private ObjectMapper objectMapper;

    /**
     * Creates an instance of the client and setups up required state
     *
     * @param url
     * @param username
     * @param password
     */
    public TestRailClient(String url, String username, String password) {
        client = new APIClient(url, username, password);
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //TODO: should probably remove this
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /*
    Plans
     */

    public Plan getPlan(int planId) throws IOException, ClientException {
        /*
        /get_plan/6
        {
            "id": 6,
            "name": "A test plan for 1.9",
            "description": null,
            "milestone_id": 1,
            "assignedto_id": null,
            "is_completed": false,
            "completed_on": null,
            "passed_count": 3,
            "blocked_count": 5,
            "untested_count": 12,
            "retest_count": 0,
            "failed_count": 0,
            "custom_status1_count": 2,
            "custom_status2_count": 0,
            "custom_status3_count": 0,
            "custom_status4_count": 0,
            "custom_status5_count": 0,
            "custom_status6_count": 0,
            "custom_status7_count": 0,
            "project_id": 1,
            "created_on": 1408648102,
            "created_by": 1,
            "url": "https://mytest1010.testrail.com/index.php?/plans/view/6",
            "entries": [
                {
                    "id": "17ed9241-7b20-474a-b66f-72cf11ffe88e",
                    "suite_id": 1,
                    "name": "A Test Suite",
                    "runs": [
                        {
                            "id": 10,
                            "suite_id": 1,
                            "name": "A Test Suite",
                            "description": null,
                            "milestone_id": 1,
                            "assignedto_id": 1,
                            "include_all": true,
                            "is_completed": false,
                            "completed_on": null,
                            "passed_count": 3,
                            "blocked_count": 0,
                            "untested_count": 4,
                            "retest_count": 0,
                            "failed_count": 0,
                            "custom_status1_count": 2,
                            "custom_status2_count": 0,
                            "custom_status3_count": 0,
                            "custom_status4_count": 0,
                            "custom_status5_count": 0,
                            "custom_status6_count": 0,
                            "custom_status7_count": 0,
                            "project_id": 1,
                            "plan_id": 6,
                            "entry_index": 1,
                            "entry_id": "17ed9241-7b20-474a-b66f-72cf11ffe88e",
                            "config": "TupleStore-Off",
                            "config_ids": [
                                1
                            ],
                            "url": "https://mytest1010.testrail.com/index.php?/runs/view/10"
                        }
                    ]
                },
                {
                    "id": "9dc80e1f-df50-4956-89c9-4477c368d5cb",
                    "suite_id": 1,
                    "name": "A Test Suite",
                    "runs": [
                        {
                            "id": 11,
                            "suite_id": 1,
                            "name": "A Test Suite",
                            ...
                            ...
                        }
                    ]
                },
                {
                    "id": "7dc87478-504d-4a3e-9fbe-e30913319065",
                    "suite_id": 2,
                    "name": "A Test Suite 2",
                    "runs": [
                        {
                            "id": 9,
                            "suite_id": 2,
                            "name": "A Test Suite 2",
                            "description": null,
                            ...
                            ...
                            "project_id": 1,
                            "plan_id": 6,
                            "entry_index": 3,
                            "entry_id": "7dc87478-504d-4a3e-9fbe-e30913319065",
                            "config": null,
                            "config_ids": [],
                            "url": "https://mytest1010.testrail.com/index.php?/runs/view/9"
                        }
                    ]
                }
            ]
        }
         */
        return objectMapper.readValue(client.invokeHttpGet("get_plan/" + planId), Plan.class);
    }

    public Plan addPlan(int projectId, String name, Integer milestoneId, List<PlanEntry> entries) throws IOException, ClientException {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("name", name);
        if (milestoneId != null) {
            body.put("milestone_id", String.valueOf(milestoneId));
        }
        if (entries != null) {
            body.put("entries", entries);
        }
        return objectMapper.readValue(
                client.invokeHttpPost("add_plan/" + projectId, objectMapper.writeValueAsString(body)), Plan.class);
    }

    public PlanEntry addPlanEntry(int planId, int suiteId) throws IOException, ClientException {
        Map<String, String> body = new HashMap<String, String>();
        body.put("suite_id", String.valueOf(suiteId));
        return objectMapper.readValue(
                client.invokeHttpPost("add_plan_entry/" + planId, objectMapper.writeValueAsString(body)), PlanEntry.class);
    }

    public Plan closePlan(int planId) throws IOException, ClientException {
        return objectMapper.readValue(client.invokeHttpPost("close_plan/" + planId, ""), Plan.class);
    }

    public void deletePlan(int planId) throws IOException, ClientException {
        client.invokeHttpPost("delete_plan/" + planId, "");
    }

    /*
    Results
     */

    /**
     *
     * @return
     */
    public List<Result> getResultsForCase(int runId, int caseId) throws IOException, ClientException {
        String url = "get_results_for_case/" + runId + "/" + caseId + "&limit=10";
        return objectMapper.readValue(client.invokeHttpGet(url), new  TypeReference<List<Result>>(){});
    }

    public Result addResultForCase(int runId, int caseId, int statusId, String comment) throws IOException, ClientException {
        String url = "add_result_for_case/" + runId + "/" + caseId;
        Map<String, String> body = new HashMap<String, String>();
        body.put("status_id", String.valueOf(statusId));
        body.put("comment", comment);
        return objectMapper.readValue(client.invokeHttpPost(url, objectMapper.writeValueAsString(body)), Result.class);
    }

    /*
    Tests
     */

    public Test getTest(int testId) throws IOException, ClientException {
        return objectMapper.readValue(client.invokeHttpGet("get_test/" + testId), Test.class);
    }

    /*
    Cases
     */

    public Case addCase(int sectionId, String title, Map<String, String> fields) throws IOException, ClientException {
        Map<String, String> body = new HashMap<String, String>();
        body.put("title", title);
        if (fields != null) {
            body.putAll(fields);
        }
        return objectMapper.readValue(
                client.invokeHttpPost("add_case/" + sectionId, objectMapper.writeValueAsString(body)), Case.class);
    }

    public Case getCase(int caseId) throws IOException, ClientException {
        return objectMapper.readValue(client.invokeHttpGet("get_case/" + caseId), Case.class);
    }

    public List<Case> getCases(int projectId, int suiteId, int sectionId, Map<String, String> filters) throws IOException, ClientException {
        String url = "get_cases/" + projectId;
        if (suiteId > 0) {
            url += "&suite_id=" + suiteId;
        }
        if (sectionId > 0) {
            url += "&section_id=" + sectionId;
        }
        if (filters != null) {
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                url += "&" + entry.getKey() + "=" + entry.getValue();
            }
        }
        return objectMapper.readValue(client.invokeHttpGet(url), new TypeReference<List<Case>>(){});
    }

    /*
    Sections
     */

    public Section addSection(int projectId, String name, int parentId, int suiteId) throws IOException, ClientException {
        Map<String, String> body = new HashMap<String, String>();
        if (suiteId > 0) {
            body.put("suite_id", String.valueOf(suiteId));
        }
        if (parentId > 0) {
            body.put("parent_id", String.valueOf(parentId));
        }
        body.put("name", name);
        return objectMapper.readValue(
                client.invokeHttpPost("add_section/" + projectId, objectMapper.writeValueAsString(body)), Section.class);
    }

    /*
    Suites
     */

    public Suite addSuite(int projectId, String name) throws IOException, ClientException {
        Map<String, String> body = new HashMap<String, String>();
        body.put("name", name);
        return objectMapper.readValue(
                client.invokeHttpPost("add_suite/" + projectId, objectMapper.writeValueAsString(body)), Suite.class);
    }

    public Suite getSuite(int suiteId) throws IOException, ClientException {
        /*
        /get_suite/1
        {
            "id": 1,
            "name": "A Test Suite",
            "description": null,
            "project_id": 1,
            "url": "https://mytest1010.testrail.com/index.php?/suites/view/1"
        }
         */
        return objectMapper.readValue(client.invokeHttpGet("get_suite/" + suiteId), Suite.class);
    }

    public List<Suite> getSuites(int projectId) throws IOException, ClientException {
        /*
        /get_suites/1
        [
            {
                "id": 1,
                "name": "A Test Suite",
                "description": null,
                "project_id": 1,
                "url": "https://mytest1010.testrail.com/index.php?/suites/view/1"
            },
            {
                "id": 2,
                "name": "A Test Suite 2",
                "description": "Another test suite",
                "project_id": 1,
                "url": "https://mytest1010.testrail.com/index.php?/suites/view/2"
            },
            ...
            ...
        ]
         */
        return objectMapper.readValue(client.invokeHttpGet("get_suites/" + projectId), new TypeReference<List<Suite>>(){});
    }

    /*
    Runs
     */

    public Run getRun(int runId) throws IOException, ClientException {
        /*
        /get_run/5
        {
            "id": 5,
            "suite_id": 2,
            "name": "A Test Suite 2 Run2",
            "description": null,
            "milestone_id": 1,
            "assignedto_id": 1,
            "include_all": true,
            "is_completed": false,
            "completed_on": null,
            "config": null,
            "config_ids": [],
            "passed_count": 1,
            "blocked_count": 0,
            "untested_count": 0,
            "retest_count": 1,
            "failed_count": 2,
            "custom_status1_count": 0,
            "custom_status2_count": 0,
            "custom_status3_count": 0,
            "custom_status4_count": 0,
            "custom_status5_count": 0,
            "custom_status6_count": 0,
            "custom_status7_count": 0,
            "project_id": 1,
            "plan_id": null,
            "created_on": 1408647332,
            "created_by": 1,
            "url": "https://mytest1010.testrail.com/index.php?/runs/view/5"
        }
         */
        return objectMapper.readValue(client.invokeHttpGet("get_run/" + runId), Run.class);
    }

}
