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
import com.nullin.testrail.dto.Milestone;
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
        return objectMapper.readValue(client.invokeHttpGet("get_plan/" + planId), Plan.class);
    }

    public Plan addPlan(int projectId, String name, Integer milestoneId, List<PlanEntry> entries)
            throws IOException, ClientException {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("name", name);
        if (milestoneId != null) {
            body.put("milestone_id", String.valueOf(milestoneId));
        }
        if (entries != null) {
            body.put("entries", entries);
        }
        return objectMapper.readValue(
                client.invokeHttpPost("add_plan/" + projectId, objectMapper.writeValueAsString(body)),
                Plan.class);
    }

    public PlanEntry addPlanEntry(int planId, int suiteId) throws IOException, ClientException {
        Map<String, String> body = new HashMap<String, String>();
        body.put("suite_id", String.valueOf(suiteId));
        return objectMapper.readValue(
                client.invokeHttpPost("add_plan_entry/" + planId, objectMapper.writeValueAsString(body)),
                PlanEntry.class);
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

    public Result addResultForCase(int runId, int caseId, int statusId, String comment)
            throws IOException, ClientException {
        String url = "add_result_for_case/" + runId + "/" + caseId;
        Map<String, String> body = new HashMap<String, String>();
        body.put("status_id", String.valueOf(statusId));
        body.put("comment", comment);
        return objectMapper.readValue(client.invokeHttpPost(url, objectMapper.writeValueAsString(body)), Result.class);
    }

    public Result addResultForCase(int runId, int caseId, Map<String, Object> properties)
            throws IOException, ClientException {
        String url = "add_result_for_case/" + runId + "/" + caseId;
        return objectMapper.readValue(client.invokeHttpPost(url, objectMapper.writeValueAsString(properties)), Result.class);
    }

    /*
    Tests
     */

    public Test getTest(int testId) throws IOException, ClientException {
        return objectMapper.readValue(client.invokeHttpGet("get_test/" + testId), Test.class);
    }

    public List<Test> getTests(int runId) throws IOException, ClientException {
        return objectMapper.readValue(client.invokeHttpGet("get_tests/" + runId), new TypeReference<List<Test>>(){});
    }

    /*
    Cases
     */

    public Case addCase(int sectionId, String title, Map<String, String> fields)
            throws IOException, ClientException {
        Map<String, String> body = new HashMap<String, String>();
        body.put("title", title);
        if (fields != null) {
            body.putAll(fields);
        }
        return objectMapper.readValue(
                client.invokeHttpPost("add_case/" + sectionId, objectMapper.writeValueAsString(body)),
                Case.class);
    }

    public Case getCase(int caseId) throws IOException, ClientException {
        return objectMapper.readValue(client.invokeHttpGet("get_case/" + caseId), Case.class);
    }

    public List<Case> getCases(int projectId, int suiteId, int sectionId, Map<String, String> filters)
            throws IOException, ClientException {
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

    public List<Map<String, Object>> getCasesAsMap(int projectId, int suiteId, int sectionId, Map<String, String> filters)
                throws IOException, ClientException {
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
        return objectMapper.readValue(client.invokeHttpGet(url), new TypeReference<List<Map<String, Object>>>(){});
    }

    /*
    Sections
     */

    public Section addSection(int projectId, String name, int parentId, int suiteId)
            throws IOException, ClientException {
        Map<String, String> body = new HashMap<String, String>();
        if (suiteId > 0) {
            body.put("suite_id", String.valueOf(suiteId));
        }
        if (parentId > 0) {
            body.put("parent_id", String.valueOf(parentId));
        }
        body.put("name", name);
        return objectMapper.readValue(
                client.invokeHttpPost("add_section/" + projectId, objectMapper.writeValueAsString(body)),
                Section.class);
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
        return objectMapper.readValue(client.invokeHttpGet("get_suite/" + suiteId), Suite.class);
    }

    public List<Suite> getSuites(int projectId) throws IOException, ClientException {
        return objectMapper.readValue(client.invokeHttpGet("get_suites/" + projectId),
                new TypeReference<List<Suite>>(){});
    }

    /*
    Milestones
     */

    public Milestone getMilestone(int milestoneId) throws IOException, ClientException {
        return objectMapper.readValue(client.invokeHttpGet("get_milestone/" + milestoneId), Milestone.class);
    }

    public List<Milestone> getMilestones(int projectId) throws IOException, ClientException {
        return objectMapper.readValue(client.invokeHttpGet("get_milestones/" + projectId),
                new TypeReference<List<Milestone>>(){});
    }

    /*
    Runs
     */

    public Run getRun(int runId) throws IOException, ClientException {
        return objectMapper.readValue(client.invokeHttpGet("get_run/" + runId), Run.class);
    }

}
