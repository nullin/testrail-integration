package com.nullin.testrail.sampleproj.pkg;

import com.nullin.testrail.annotations.TestRailCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TODO: add javadocs!
 *
 * @author nullin
 */
public class TestClassC {

    @TestRailCase(automationId = "35")
    @Test
    public void test1() {
        // do nothing always passes
    }

    @TestRailCase(automationId = "38")
    @Test
    public void test4() {
        Assert.fail("Always fails!!");
    }
}
