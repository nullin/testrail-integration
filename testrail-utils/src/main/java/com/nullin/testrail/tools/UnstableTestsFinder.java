package com.nullin.testrail.tools;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nullin.testrail.client.ClientException;
import com.nullin.testrail.client.TestRailClient;
import com.nullin.testrail.dto.*;


import java.io.IOException;
import java.util.*;

/**
 * Attempts to find unstable tests by looking at past test results.
 *
 * Milestone name is used to find associated TestPlans and then we get all test results for each test case (based on
 * automation id) and finally check if we see unstable results for the tests.
 *
 * @author nullin
 */
public class UnstableTestsFinder {

    /**
     * currently takes 3 args:
     *
     * {testrail URL} {testrail user} {testrail passwd}
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        TestRailClient client = new TestRailClient(args[0], args[1], args[2]);

        int projectId = 1;
        String milestoneName = "master"; //TODO: make configurable

        List<Milestone> milestones = client.getMilestones(projectId);

        int milestoneId = -1;
        for (Milestone milestone : milestones) {
            if (milestone.name.equals(milestoneName)) {
                milestoneId = milestone.id;
            }
        }

        Map<String, String> filters = new HashMap<>();
        filters.put("milestone_id", String.valueOf(milestoneId));
        filters.put("limit", "5"); //TODO: make configurable
        List<Plan> plans = client.getPlans(projectId, filters);

        Map<Integer, String> automationIdMap = Maps.newHashMap();
        Map<Integer, List<Result>> resultMap = Maps.newLinkedHashMap();

        for (Plan plan : plans) {
            System.out.println("Plan: " + plan.name);
            List<PlanEntry> planEntries = client.getPlan(plan.id).entries;
            for (PlanEntry planEntry : planEntries) {
                for (Run run : planEntry.runs) {
                    List<Result> results = getResults(client, run.id);
                    List<Test> tests = client.getTests(run.id);
                    System.out.println("Run: " + run.name + ", Tests Size: " + tests.size() + ", Results Size: " + results.size());
                    Map<Integer, List<Result>> testResultMap = new LinkedHashMap<>();

                    for (Result result : results) {
                        if (result.statusId != null) {
                            if (testResultMap.get(result.testId) != null) {
                                testResultMap.get(result.testId).add(result);
                            } else {
                                testResultMap.put(result.testId, Lists.newArrayList(result));
                            }
                        }
                    }
                    for (Test test : tests) {
                        if (!automationIdMap.containsKey(test.caseId)) {
                            automationIdMap.put(test.caseId, test.automationId);
                        }

                        List<Result> testResults = testResultMap.get(test.id);
                        if (testResults != null) {
                            if (resultMap.get(test.caseId) != null) {
                                resultMap.get(test.caseId).addAll(testResults);
                            } else {
                                resultMap.put(test.caseId, testResults);
                            }
                        }
                    }
                }
            }
        }

        for (Map.Entry<Integer, List<Result>> entry : resultMap.entrySet()) {
            List<Result> results = entry.getValue();
            List<Integer> statuses = Lists.transform(results, new Function<Result, Integer>() {
                @Override
                public Integer apply(Result result) {
                    return result.statusId;
                }
            });
            boolean isUnstable = check(statuses);
            if (isUnstable) {
                System.out.println("Case " + entry.getKey() + " (" + automationIdMap.get(entry.getKey()) + "): " + statuses);
            }
        }

    }

    private static List<Result> getResults(TestRailClient client, Integer id) throws IOException, ClientException {
        Map<String, String> filters = Maps.newHashMap();
        List<Result> results = Lists.newArrayList();
        List<Result> tmp;
        int offset = 250;
        while (!(tmp = client.getResultsForRun(id, filters)).isEmpty()) {
            results.addAll(tmp);
            filters.put("offset", String.valueOf(offset));
            offset += 250;
        }
        return results;
    }

    private static boolean check(List<Integer> statuses) {
        int count = 0;
        for (int i = 0 ; i < statuses.size() - 1; i++) {
            if (!statuses.get(i).equals(statuses.get(i + 1))) {
                count++;
            }
        }
        return count >= 3;
    }

}