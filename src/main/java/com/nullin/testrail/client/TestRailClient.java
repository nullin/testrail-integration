package com.nullin.testrail.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nullin.testrail.dto.Case;
import com.nullin.testrail.dto.Plan;
import com.nullin.testrail.dto.PlanEntry;
import com.nullin.testrail.dto.Result;
import com.nullin.testrail.dto.Run;
import com.nullin.testrail.dto.Suite;
import com.nullin.testrail.dto.Test;

/**
 * TODO: add javadocs!
 *
 * @author nullin
 */
public class TestRailClient {

    private APIClient client;
    private ObjectMapper objectMapper;

    public TestRailClient(String url, String username, String password) {
        client = new APIClient(url, username, password);
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    //Plans

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

    public Plan addPlan(int projectId, String name, Integer milestoneId) throws IOException, ClientException {
        Map<String, String> body = new HashMap<String, String>();
        body.put("name", name);
        if (milestoneId != null) {
            body.put("milestone_id", String.valueOf(milestoneId));
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

    //result

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

    //test

    public Test getTest(int testId) throws IOException, ClientException {
        return objectMapper.readValue(client.invokeHttpGet("get_test/" + testId), Test.class);
    }

    //case

    public Case getCase(int caseId) throws IOException, ClientException {
        return objectMapper.readValue(client.invokeHttpGet("get_case/" + caseId), Case.class);
    }

    //Suite

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

    //Run

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
