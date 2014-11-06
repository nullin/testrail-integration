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
import org.testng.ITestResult;

/**
 * A TestNG listener to report results to TestRail instance
 *
 * @author nullin
 */
public class TestRailReporter {

    public enum ResultStatus {
        PASS, FAIL, SKIP
    }

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
     * Reports the result for the test method to TestRail
     *
     */
    public void reportResult(String automationId, ResultStatus resultStatus, Throwable throwable) {
        if (!enabled) {
            return; //do nothing
        }

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

            //add the result
            client.addResultForCase(run.id, caseId, getStatus(resultStatus), comment);
        } catch(Exception ex) {
            //only log and do nothing else
            logger.severe("Ran into exception " + ex.getMessage());
        }
    }

    /**
     * Reports the result for the test method to TestRail
     *
     */
    public void reportResult(String automationId, int status, Throwable throwable) {
        if (!enabled) {
            return; //do nothing
        }

        try {
            Integer caseId = caseIdLookupMap.get(automationId);
            if (caseId == null) {
                logger.severe("Didn't find case id for test with automation id " + automationId);
                return; //nothing more to do
            }

            String comment = null;
            if (status == ITestResult.FAILURE
                    || status == ITestResult.SUCCESS_PERCENTAGE_FAILURE) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                throwable.printStackTrace(new PrintStream(os));
                comment = os.toString();
            }

            //add the result
            client.addResultForCase(run.id, caseId, getStatus(status), comment);
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

    /**
     * @param status TestNG specific status code
     * @return TestRail specific status IDs
     */
    private int getStatus(int status) {
        switch (status) {
            case ITestResult.SUCCESS:
                return 1; //Passed
            case ITestResult.FAILURE:
                return 5; //Failed
            case ITestResult.SKIP:
                return 2; //Blocked
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                return 5; //Failed
            default:
                return 3; //Untested
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

}
