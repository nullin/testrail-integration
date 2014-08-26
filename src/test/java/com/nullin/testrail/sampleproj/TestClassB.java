package com.nullin.testrail.sampleproj;

import com.nullin.testrail.annotations.TestRailCase;
import com.nullin.testrail.annotations.TestRailClass;
import org.testng.annotations.Test;

/**
 * TODO: add javadocs!
 *
 * @author nullin
 */
@TestRailClass(suiteName = "TestSuite1")
public class TestClassB {

    @TestRailCase(caseId = "34")
    @Test
    public void test1() {
        // do nothing always passes
    }

}
