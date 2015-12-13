package com.nullin.testrail;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.nullin.testrail.annotations.TestRailCase;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;

/**
 * A TestNG listener to report results to TestRail instance
 *
 * @author nullin
 */
public class TestRailListener implements ITestListener, IConfigurationListener {

    private Logger logger = Logger.getLogger(TestRailListener.class.getName());

    private TestRailReporter reporter;
    private boolean enabled;

    /**
     * Store the result associated with a failed configuration here. This can
     * then be used when reporting the result of a skipped test to provide
     * additional information in TestRail
     */
    private ThreadLocal<ITestResult> testSkipResult = new ThreadLocal<ITestResult>();

    public TestRailListener() {
        try {
            reporter = TestRailReporter.getInstance();
            enabled = reporter.isEnabled();
        } catch (Throwable ex) {
            logger.severe("Ran into exception initializing reporter: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Reports the result for the test method to TestRail
     * @param result TestNG test result
     */
    private void reportResult(ITestResult result) {
        if (!enabled) {
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
            Test test = method.getAnnotation(Test.class);
            String automationId;
            if (trCase == null) {
                if (null != test.dataProvider() && !test.dataProvider().isEmpty()) {
                    if (firstParam == null) {
                        logger.severe("Didn't find the first parameter for DD test " + id + ". Result not reported.");
                        return; //nothing more to do
                    }
                    automationId = firstParam;
                } else {
                    logger.severe(String.format("Test case %s is not annotated with TestRailCase annotation. " +
                            "Result not reported", id));
                    return; //nothing more to do
                }
            } else {
                automationId = trCase.value();
            }

            if (automationId == null || automationId.isEmpty()) {
                //case id not specified on method, check if this is a DD method
                if (!trCase.selfReporting()) {
                    //self reporting test cases are responsible of reporting results on their own
                    logger.warning("Didn't find automation id nor is the test self reporting for test " + id +
                            ". Please check test configuration.");
                    return; //nothing more to do
                } else {
                    return; //nothing to do as the test is marked as self reporting
                }
            }

            Map<String, Object> props = new HashMap<String, Object>();
            long elapsed = (result.getEndMillis() - result.getStartMillis()) / 1000;
            elapsed = elapsed == 0 ? 1 : elapsed; //we can only track 1 second as the smallest unit
            props.put("elapsed",  elapsed + "s");
            props.put("status", getStatus(status));
            props.put("throwable", throwable);
            //override if needed
            if (status == ITestResult.SKIP) {
                ITestResult skipResult = testSkipResult.get();
                if (skipResult != null) {
                    props.put("throwable", skipResult.getThrowable());
                }
            }
            props.put("screenshotUrl", getScreenshotUrl(result));
            Map<String, String> moreInfo = new LinkedHashMap<String, String>();
            moreInfo.put("class", result.getMethod().getRealClass().getCanonicalName());
            moreInfo.put("method", result.getMethod().getMethodName());
            if (result.getParameters() != null) {
                moreInfo.put("parameters", Arrays.toString(result.getParameters()));
            }
            moreInfo.putAll(getMoreInformation());
            props.put("moreInfo", moreInfo);
            reporter.reportResult(automationId, props);
        } catch(Exception ex) {
            //only log and do nothing else
            logger.severe("Ran into exception " + ex.getMessage());
        }
    }

    public void onTestStart(ITestResult result) {
        //not reporting a started status
    }

    public void onTestSuccess(ITestResult result) {
        reportResult(result);
    }

    public void onTestFailure(ITestResult result) {
        reportResult(result);
    }

    public void onTestSkipped(ITestResult result) {
        if (result.getThrowable() != null) {
            //test failed, but is reported as skipped because of RetryAnalyzer.
            //so, changing result status and reporting this as failure instead.
            result.setStatus(ITestResult.FAILURE);
        }
        reportResult(result);
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        //nothing here
    }

    public void onStart(ITestContext context) {
        //nothing here
    }

    public void onFinish(ITestContext context) {
        //nothing here
    }

    /**
     * TestRail currently doesn't support uploading screenshots via APIs. Suggested method is
     * to upload screenshots to another server and provide a URL in the test comments.
     *
     * This method should be overridden in a sub-class to provide the URL for the screenshot.
     *
     * @param result result of test execution
     * @return the URL to where the screenshot can be accessed
     */
    public String getScreenshotUrl(ITestResult result) {
        return null; //should be extended & overridden if needed
    }

    /**
     * In case, we want to log more information about the test execution, this method can be used.
     *
     * NOTE: the test class/method/parameter information is automatically logged.
     *
     * This method should be overridden in a sub-class to provide map containing information
     * that should be displayed for each test result in TestRail
     */
    public Map<String, String> getMoreInformation() {
        return Collections.emptyMap(); //should be extended & overridden if needed
    }

    /**
     * @param status TestNG specific status code
     * @return TestRail specific status IDs
     */
    private ResultStatus getStatus(int status) {
        switch (status) {
            case ITestResult.SUCCESS:
                return ResultStatus.PASS;
            case ITestResult.FAILURE:
                return ResultStatus.FAIL;
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                return ResultStatus.FAIL;
            case ITestResult.SKIP:
                return ResultStatus.SKIP;
            default:
                return ResultStatus.UNTESTED;
        }
    }

    public void onConfigurationSuccess(ITestResult iTestResult) {
        testSkipResult.remove();
    }

    public void onConfigurationFailure(ITestResult iTestResult) {
        testSkipResult.set(iTestResult);
    }

    public void onConfigurationSkip(ITestResult iTestResult) {
        //nothing here
    }
}
