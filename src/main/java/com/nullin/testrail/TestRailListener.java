package com.nullin.testrail;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nullin.testrail.annotations.TestRailCase;
import com.nullin.testrail.annotations.TestRailClass;
import com.nullin.testrail.client.ClientException;
import com.nullin.testrail.client.TestRailClient;
import com.nullin.testrail.dto.Plan;
import com.nullin.testrail.dto.PlanEntry;
import com.nullin.testrail.dto.Result;
import com.nullin.testrail.dto.Suite;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * A simple listener that starts up a server and prints out the current execution status.
 *
 * Uses jetty to start the server and handle the requests and uses a h2 in-memory data base
 * to store the results
 *
 * @author nullin
 */
public class TestRailListener implements ITestListener {

    private TestRailListenerArgs args;
    private TestRailClient client;
    private Boolean enabled;
    private Map<String, Integer> suiteMap;
    private Plan plan;

    public TestRailListener() throws IOException, ClientException {
        args = TestRailListenerArgs.getNewTestRailListenerArgs();
        enabled = args.getEnabled();

        if (enabled != null && enabled) {
            client = new TestRailClient(args.getUrl(), args.getUsername(), args.getPassword());

            //TODO: check that we are able to login


            //prepare the test plan and stuff
            int projectId = args.getProjectId();
            suiteMap = getSuiteMap(client.getSuites(projectId));

            if (args.getTestPlanId() != null) {
                //Check TestRunId is valid if specified
                plan = client.getPlan(args.getTestPlanId());
            } else {
                //create a new test run
                //TODO: add args for test run name + milestone id
                plan = client.addPlan(projectId, "Test Plan " + new Date(), null);
            }
        }
    }

    private Map<String, Integer> getSuiteMap(List<Suite> suites) {
        Map<String, Integer> suiteMap = new HashMap<String, Integer>(suites.size());
        for (Suite suite : suites) {
            suiteMap.put(suite.name, suite.id);
        }
        return suiteMap;
    }

    private PlanEntry findPlanEntry(List<PlanEntry> entries, int suiteId) {
        for (PlanEntry planEntry : entries) {
            if (planEntry.suiteId == suiteId) {
                return planEntry;
            }
        }
        return null;
    }

    private void reportReult(ITestResult result) {
        if (enabled == null || !enabled) {
            return; //do nothing
        }

        Class clazz = result.getTestClass().getRealClass();
        TestRailClass trClass = (TestRailClass) clazz.getAnnotation(TestRailClass.class);
        if (trClass == null) {
            //TODO: log to file
            return; //nothing more to do
        }

        String suiteName = trClass.suiteName();
        Integer suiteId = suiteMap.get(suiteName);

        if (suiteId == null) {
            //TODO: log to file
            return; //nothing more to do
        }

        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        TestRailCase trCase = method.getAnnotation(TestRailCase.class);
        if (trCase == null) {
            //TODO: log to file
            return; //nothing more to do
        }

        String classname = result.getTestClass().getName();
        String methodname = result.getMethod().getMethodName();
        String id = classname + "#" + methodname;
        Object[] params = result.getParameters();
        String firstParam = null;
        if (params != null && params.length > 0) {
            id += "(" + params[0] + ")";
            firstParam = String.valueOf(params[0]);
        }
        int status = result.getStatus();
        Throwable throwable = result.getThrowable();
        String exception = throwable == null ? null : throwable.toString();

        String caseIdStr = trCase.caseId();
        if (caseIdStr == null || caseIdStr.isEmpty()) {
            //case id not specified on method, check if this is a DD method
            if (trCase.dataDriven()) {
                if (firstParam == null) {
                    //TODO: log to file
                    return; //nothing more to do
                }
                caseIdStr = firstParam;
            } else {
                //TODO: log to file
                return; //nothing more to do
            }
        }

        Integer caseId = null;
        try {
            caseId = Integer.valueOf(caseIdStr);
        } catch (NumberFormatException ex) {
            //TODO: log to file
            return; //nothing more to do
        }

        if (caseId == null) {
            //TODO: log to file
            return; //nothing more to do
        }

        try {
            plan = client.getPlan(plan.id);

            PlanEntry planEntry = findPlanEntry(plan.entries, suiteId);
            if (planEntry == null) {
                //we didn't find one for this suite, so we are adding it
                planEntry = client.addPlanEntry(plan.id, suiteId);
            }

            //associated test run id
            int runId = planEntry.runs.get(0).id;

            String comment = "Test Passed";
            if (status == ITestResult.FAILURE
                    || status == ITestResult.SUCCESS_PERCENTAGE_FAILURE) {
                comment = exception;
            }

            Result trResult = client.addResultForCase(runId, caseId, getStatus(status), comment);
        } catch(Exception ex) {

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

    @Override
    public void onTestStart(ITestResult result) {
        //not reporting a started status
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        reportReult(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        reportReult(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        reportReult(result);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        //nothing here
    }

    @Override
    public void onStart(ITestContext context) {
        //nothing here
    }

    @Override
    public void onFinish(ITestContext context) {
        //nothing here
    }

}
