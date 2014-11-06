package com.nullin.testrail.extra;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nullin.testrail.client.ClientException;
import com.nullin.testrail.client.TestRailClient;
import com.nullin.testrail.dto.Case;
import com.nullin.testrail.dto.PlanEntry;
import com.nullin.testrail.dto.Run;
import com.nullin.testrail.dto.Suite;

/**
 * Test the implementation of our API calls
 *
 * @author nullin
 */
public class TestTheListener {

    public static void main(String[] args) throws ClientException, IOException {

        TestRailClient client = new TestRailClient(args[0], args[1], args[2]);

        int projectId = 6; //should be read from a configuration
        /*
        - Requirements:
            - All TestSuite names should be unique
            - Add annotations to class and test level
            - Add caseIds to DD test data as first parameter

        Workflow:
        - Connect and get a list of all test suites in the project and cache in a map
        - Create a new test plan (if an id isn't already specified)
        - For each result to be reported, get the method and it's enclosing class
            - get the annotations
            - find if test plan already has a test run for the specified suite
            - if not, create a new test run for the suite, and the get the test run id
                (multiple runs for same suite, not supported yet. Need to handle configurationIds for that to work)
            - if the test was DD, the first parameter should be case id
            - now we have case id and test run id
            - report the result
        - Done with all results, close the test plan (by default or control based
                                                      on user specified parameter)
        - DONE

        - extras:
            - support creating test runs with configurations
            - output tests that were not reported into file w/ reason for failure
         */

        List<Suite> suiteList = client.getSuites(projectId);
        Map<String, Integer> suiteMap = getSuiteMap(suiteList);
        System.out.println("Suite Map: " + suiteMap);

        //Plan plan = client.getPlan(6);
//        Plan plan = client.addPlan(projectId, "Test Plan_" + new Date().toString(), null);

        try {
            int suiteId = suiteMap.get("Master");
            int caseId = 226;

//            Assert.assertEquals(plan.entries.size(), 0, "Plan Entries");

            List<Case> cases = client.getCases(projectId, 0, 0, null);
            System.out.println(cases.size());
//            int caseId = findCase(cases, "test1");
//            Assert.assertEquals(caseId, 226, "Case id");

//            PlanEntry planEntry = findPlanEntry(plan.entries, suiteId);
//            if (planEntry == null) {
//                planEntry = client.addPlanEntry(plan.id, suiteId);
//            }
//
//            //int runId = getRunId(plan.entries, suiteId);
//            int runId = planEntry.runs.get(0).id;
//
//            Result result = client.addResultForCase(runId, caseId, 1, "Nalin Added this comment Yo!! Yolo!");
//            System.out.println(result);
        } finally {
            //client.closePlan(plan.id); // don't complete, because completed plans can't be deleted
//            client.deletePlan(plan.id);
        }
        System.out.println("");
    }

    private static int findCase(List<Case> cases, String test1) {
        for (Case c : cases) {
            if (c.automationId != null && c.automationId.equals(test1)) {
                return c.id;
            }
        }
        return -1;
    }

    private static PlanEntry findPlanEntry(List<PlanEntry> entries, int suiteId) {
        for (PlanEntry planEntry : entries) {
            if (planEntry.suiteId == suiteId) {
                return planEntry;
            }
        }
        return null;
    }

    private static int getRunId(List<PlanEntry> entries, int suiteId) {
        for (PlanEntry planEntry : entries) {
            if (planEntry.suiteId == suiteId) {
                List<Run> runs = planEntry.runs;
                return runs.get(0).id;
            }
        }
        throw new IllegalArgumentException("Didn't find entry for suite " + suiteId);
    }

    private static Map<String, Integer> getSuiteMap(List<Suite> suites) {
        Map<String, Integer> suiteMap = new HashMap<String, Integer>(suites.size());
        for (Suite suite : suites) {
            suiteMap.put(suite.name, suite.id);
        }
        return suiteMap;
    }

}
