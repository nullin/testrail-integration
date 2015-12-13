package com.nullin.testrail.sampleproj.pkg;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.nullin.testrail.ResultStatus;
import com.nullin.testrail.TestRailReporter;
import com.nullin.testrail.annotations.TestRailCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author nullin
 */
public class TestClassC {

    @TestRailCase("testC1")
    @Test
    public void test1() {
        // do nothing always passes
    }

    @TestRailCase("testC2")
    @Test
    public void test4() {
        Assert.fail("Always fails!!");
    }

    @TestRailCase(selfReporting = true)
    @Test
    public void test5() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(TestRailReporter.KEY_STATUS, ResultStatus.PASS);
        TestRailReporter.getInstance().reportResult("testC3", result);
        result.put(TestRailReporter.KEY_STATUS, ResultStatus.FAIL);
        result.put(TestRailReporter.KEY_THROWABLE, new IOException("Something very bad happened!!"));
        TestRailReporter.getInstance().reportResult("testC4", result);
    }
}
