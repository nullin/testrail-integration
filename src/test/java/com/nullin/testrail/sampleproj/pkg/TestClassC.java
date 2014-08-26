package com.nullin.testrail.sampleproj.pkg;

import com.nullin.testrail.annotations.TestRailCase;
import com.nullin.testrail.annotations.TestRailClass;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TODO: add javadocs!
 *
 * @author nullin
 */
@TestRailClass(suiteName = "TestSuite2")
public class TestClassC {

    @TestRailCase(caseId = "35")
    @Test
    public void test1() {
        // do nothing always passes
    }

    @TestRailCase(caseId = "38")
    @Test
    public void test4() {
        Assert.fail("Always fails!!");
    }
}
