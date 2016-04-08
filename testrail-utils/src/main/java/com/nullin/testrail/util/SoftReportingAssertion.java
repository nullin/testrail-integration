package com.nullin.testrail.util;

import java.util.HashMap;
import java.util.Map;

import com.nullin.testrail.ResultStatus;
import com.nullin.testrail.TestRailReporter;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

/**
 * Soft Reporting Assertion class extends the functionality of {@link org.testng.asserts.SoftAssert} to
 * allow us to report results to TestRail using {@link com.nullin.testrail.TestRailReporter}.
 *
 * There are still issues here. If the test is not written correctly and there
 * are exceptions raised, we could have a situation where this error is not reported correctly to
 * TestRail. We are relying on the fact that there are no exceptions raised during the running of the
 * test method, except when {@code assertAll()} method is invoked.
 *
 * Usage:
 * <ul>
 *     <li>Should be used only with tests with {@link com.nullin.testrail.annotations.TestRailCase} annotation
 *     marking the test as Self Reporting</li>
 *     <li>Create an instance of this class for each test that needs to be reported on</li>
 *     <li>Use that instance for running all asserts associated with a given test</li>
 *     <li>At end of the overall test, use {@link SoftReportingAssertion#assertAll()} to run {@code assertAll()} methods
 *     for all these instances in the finally block.</li>
 * </ul>
 *
 * @author nullin
 */
public class SoftReportingAssertion extends SoftAssert {

    //Automation Id for test to be reported on
    private String testAutomationId;
    private boolean hasFailedBefore;
    private boolean hasPassedBefore;
    private boolean hasRunAtLeastOnce;

    public SoftReportingAssertion(String testAutomationId) {
        this.testAutomationId = testAutomationId;
    }

    @Override
    public void onAssertSuccess(IAssert iAssert) {
        hasRunAtLeastOnce = true;
        super.onAssertSuccess(iAssert);
        if (!hasFailedBefore && !hasPassedBefore) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(TestRailReporter.KEY_STATUS, ResultStatus.PASS);
            properties.put(TestRailReporter.KEY_MORE_INFO, getMoreInfo());
            TestRailReporter.getInstance().reportResult(testAutomationId, properties);
            hasPassedBefore = true;
        }
    }

    @Override
    public void onAssertFailure(IAssert iAssert, AssertionError ex) {
        hasRunAtLeastOnce = true;
        super.onAssertFailure(iAssert, ex);
        Map<String, Object> properties = new HashMap<>();
        properties.put(TestRailReporter.KEY_STATUS, ResultStatus.FAIL);
        properties.put(TestRailReporter.KEY_THROWABLE, ex);
        properties.put(TestRailReporter.KEY_MORE_INFO, getMoreInfo());
        TestRailReporter.getInstance().reportResult(testAutomationId, properties);
        hasFailedBefore = true;
    }

    private Map<String, String> getMoreInfo() {
        return getMoreInfo(6);
    }

    private Map<String, String> getMoreInfo(int stackTraceElIdx) {
        Map<String, String> moreInfo = new HashMap<>();
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[stackTraceElIdx];
        moreInfo.put("class", stackTraceElement.getClassName());
        moreInfo.put("method", stackTraceElement.getMethodName());
        return moreInfo;
    }

    private boolean hasRunAtLeastOnce() {
        return hasRunAtLeastOnce;
    }

    private String getTestAutomationId() {
        return testAutomationId;
    }

    /**
     * Helper method that runs the {@code assertAll()} method on all the instances passed in
     * <p>
     * Note: This methods throws AssertionError so it should be called in try-finally and
     * all cleanups are done in finally block.
     * <pre>
     *     try {
     *         test code
     *     } finally {
     *         try {
     *             SoftReportingAssertion.assertAll(assert_1, assert_2, assert_3);
     *         } finally {
     *             cleanup code
     *         }
     *     }
     * </pre>
     * </p>
     * @param asserts test asserts
     */
    public static void assertAll(SoftReportingAssertion... asserts) {
        StringBuilder allAssertsMessages = new StringBuilder();
        for (SoftReportingAssertion sr_assert : asserts) {
            if (sr_assert.hasRunAtLeastOnce()) {
                try {
                    sr_assert.assertAll();
                } catch (AssertionError assertionError) {
                    allAssertsMessages.append("\n").append(sr_assert.getTestAutomationId())
                            .append("\n\t").append(assertionError.getMessage()).append("\n");
                }
            } else {
                //not been invoked at all
                Map<String, Object> properties = new HashMap<>();
                properties.put(TestRailReporter.KEY_STATUS, ResultStatus.SKIP);
                properties.put(TestRailReporter.KEY_THROWABLE,
                        new AssertionError(sr_assert.getTestAutomationId() + " assert was not executed"));
                properties.put(TestRailReporter.KEY_MORE_INFO, sr_assert.getMoreInfo(3));
                TestRailReporter.getInstance().reportResult(sr_assert.getTestAutomationId(), properties);
            }
        }

        String allAssertMsgsStr = allAssertsMessages.toString();
        if (!allAssertMsgsStr.isEmpty()) {
            throw new AssertionError(allAssertsMessages.toString());
        }
    }

    @Override
    public void assertAll() {
        super.assertAll();
    }
}
