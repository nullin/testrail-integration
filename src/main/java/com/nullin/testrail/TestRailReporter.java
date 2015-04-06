package com.nullin.testrail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.nullin.testrail.client.ClientException;
import com.nullin.testrail.client.TestRailClient;
import com.nullin.testrail.dto.Case;
import com.nullin.testrail.dto.Plan;
import com.nullin.testrail.dto.PlanEntry;
import com.nullin.testrail.dto.Run;

/**
 * This class is responsible with communicating with TestRail and reporting results to it.
 *
 * It's invoked by {@link com.nullin.testrail.TestRailListener} and has methods that can be
 * directly invoked from the test code as well.
 *
 * @author nullin
 */
public class TestRailReporter {

    private Logger logger = Logger.getLogger(TestRailReporter.class.getName());
    private TestRailArgs args;
    private TestRailClient client;
    private Run run;
    private Map<String, Integer> caseIdLookupMap;
    private Boolean enabled;

    private static class Holder {
        private static final TestRailReporter INSTANCE = new TestRailReporter();
    }

    public static TestRailReporter getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Initializes the required state including any required caches.
     *
     * This method is allowed to throw exception so that we can stop test execution
     * to fix invalid configuration before proceeding further
     *
     * @throws java.io.IOException
     * @throws com.nullin.testrail.client.ClientException
     */
    private TestRailReporter() {
        args = TestRailArgs.getNewTestRailListenerArgs();
        enabled = args.getEnabled();
        enabled = enabled == null ? false : enabled;

        if (!enabled) {
            logger.info("TestRail listener is not enabled. Results will not be reported to TestRail.");
            return;
        }

        logger.info("TestRail listener is enabled. Configuring...");
        try {
            client = new TestRailClient(args.getUrl(), args.getUsername(), args.getPassword());

            //prepare the test plan and stuff
            Plan plan = client.getPlan(args.getTestPlanId());

            //check some constraints
            if (plan.entries.size() != 1) {
                throw new IllegalStateException("Referenced plan " + plan.id + " has multiple test suites." +
                        " This configuration is currently not supported.");
            }

            PlanEntry planEntry = plan.entries.get(0);
            if (planEntry.runs.size() != 1) {
                throw new IllegalStateException("Referenced plan " + plan.id + " has multiple test runs for the same suite." +
                        " This configuration is currently not supported.");
            }

            run = planEntry.runs.get(0);
            caseIdLookupMap = cacheCaseIdLookupMap(client, run);
        } catch(Exception ex) {
            //wrap in a Runtime and throw again
            //why? because we don't want to handle it and we want
            //to stop the execution asap.
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets all the test cases associated with the test run and caches a map of the
     * associated automation id's to the case ids
     *
     * @param run
     * @return
     */
    private Map<String, Integer> cacheCaseIdLookupMap(TestRailClient client, Run run)
            throws IOException, ClientException {
        List<Case> cases = client.getCases(run.projectId, run.suiteId, 0, null);
        Map<String, Integer> lookupMap = new HashMap<String, Integer>();
        for (Case c : cases) {
            if (c.automationId == null || c.automationId.isEmpty()) {
                continue; //ignore empty automation IDs
            }

            if (lookupMap.get(c.automationId) != null) {
                logger.severe("Found multiple tests cases with same automation id. " +
                        "Case Ids " + lookupMap.get(c.automationId) + " & " + c.id);
            } else {
                lookupMap.put(c.automationId, c.id);
            }
        }
        return lookupMap;
    }

    /**
     * Reports the result to TestRail
     *
     * @param automationId automation id of the test case
     * @param resultStatus status of the test after execution (or attempted execution)
     * @param throwable Any associated exception, only reported if the result status
     *                  is {@link com.nullin.testrail.ResultStatus#FAIL}
     *
     */
    @Deprecated
    public void reportResult(String automationId, ResultStatus resultStatus, Throwable throwable) {
        reportResult(automationId, resultStatus, throwable, null);
    }

    /**
     * Reports the result to TestRail
     *
     * @param automationId automation id of the test case
     * @param resultStatus status of the test after execution (or attempted execution)
     * @param throwable Any associated exception, only reported if the result status
     *                  is {@link com.nullin.testrail.ResultStatus#FAIL}
     * @param screenshotUrl URL to a screenshot if one was captured and uploaded to an external server.
     *                      This URL will be encapsulated in the associated test comment to display the
     *                      screenshot for tests with result status as {@link com.nullin.testrail.ResultStatus#FAIL}
     *
     */
    @Deprecated
    public void reportResult(String automationId, ResultStatus resultStatus, Throwable throwable, String screenshotUrl) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("status", resultStatus);
        properties.put("throwable", throwable);
        properties.put("screenshotUrl", screenshotUrl);
        reportResult(automationId, properties);
    }

    public void reportResult(String automationId, Map<String, Object> properties) {
        if (!enabled) {
            return; //do nothing
        }

        ResultStatus resultStatus = (ResultStatus)properties.get("status");
        Throwable throwable = (Throwable)properties.get("throwable");
        String elapsed = (String)properties.get("elapsed");
        String screenshotUrl = (String)properties.get("screenshotUrl");

        try {
            Integer caseId = caseIdLookupMap.get(automationId);
            if (caseId == null) {
                logger.severe("Didn't find case id for test with automation id " + automationId);
                return; //nothing more to do
            }

            String comment = null;
            if (resultStatus.equals(ResultStatus.FAIL)) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                throwable.printStackTrace(new PrintStream(os));
                comment = os.toString();
            }

            if (screenshotUrl != null && !screenshotUrl.isEmpty()) {
                comment = "![](" + screenshotUrl + ")\n\n" + comment;
            }

            //add the result
            Map<String, Object> body = new HashMap<String, Object>();
            body.put("status_id", getStatus(resultStatus));
            body.put("comment", comment);
            body.put("elapsed", elapsed);
            client.addResultForCase(run.id, caseId, body);
        } catch(Exception ex) {
            //only log and do nothing else
            logger.severe("Ran into exception " + ex.getMessage());
        }
    }

    /**
     * @param status TestNG specific status code
     * @return TestRail specific status IDs
     */
    private int getStatus(ResultStatus status) {
        switch (status) {
            case PASS:
                return 1; //Passed
            case FAIL:
                return 5; //Failed
            case SKIP:
                return 2; //Blocked
            default:
                return 3; //Untested
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

}
