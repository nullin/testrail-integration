package com.nullin.testrail.sampleproj.pkg;

import java.io.IOException;

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
        TestRailReporter.getInstance().reportResult("testC3", ResultStatus.PASS, null);
        TestRailReporter.getInstance().reportResult("testC4", ResultStatus.FAIL,
                new IOException("Something very bad happened!!"));
    }
}
