package com.nullin.testrail;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.nullin.testrail.annotations.TestRailCase;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * A TestNG listener to report results to TestRail instance
 *
 * @author nullin
 */
public class TestRailListener implements ITestListener {

    private Logger logger = Logger.getLogger(TestRailListener.class.getName());

    private TestRailReporter reporter;

    public TestRailListener() {
        reporter = TestRailReporter.getInstance();
    }

    /**
     * Reports the result for the test method to TestRail
     * @param result TestNG test result
     */
    private void reportResult(ITestResult result) {
        if (!reporter.isEnabled()) {
            return; //do nothing
        }

        try {
            Method method = result.getMethod().getConstructorOrMethod().getMethod();
            String className = result.getTestClass().getName();
            String methodName = result.getMethod().getMethodName();
            String id = className + "#" + methodName;
            Object[] params = result.getParameters();
            String firstParam = null;
            if (params != null && params.length > 0) {
                id += "(" + params[0] + ")";
                firstParam = String.valueOf(params[0]);
            }
            int status = result.getStatus();
            Throwable throwable = result.getThrowable();

            TestRailCase trCase = method.getAnnotation(TestRailCase.class);
            if (trCase == null) {
                logger.severe(String.format("Test case %s is not annotated with TestRailCase annotation. " +
                        "Result not reported", id));
                return; //nothing more to do
            }

            String automationId = trCase.automationId();
            if (automationId == null || automationId.isEmpty()) {
                //case id not specified on method, check if this is a DD method
                if (trCase.dataDriven()) {
                    if (firstParam == null) {
                        logger.severe("Didn't find the first parameter for DD test " + id + ". Result not reported.");
                        return; //nothing more to do
                    }
                    automationId = firstParam;
                } else if (!trCase.selfReporting()) {
                    //self reporting test cases are responsible of reporting results on their own
                    logger.warning("Didn't find automation id and nor is the test self reporting for test " + id +
                            ". Please check test configuration.");
                    return; //nothing more to do
                } else {
                    return; //nothing to do as the test is marked as self reporting
                }
            }

            reporter.reportResult(automationId, status, throwable);
        } catch(Exception ex) {
            //only log and do nothing else
            logger.severe("Ran into exception " + ex.getMessage());
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        //not reporting a started status
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        reportResult(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        reportResult(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        reportResult(result);
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
