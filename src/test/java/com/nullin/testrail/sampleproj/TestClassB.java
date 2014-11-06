package com.nullin.testrail.sampleproj;

import com.nullin.testrail.annotations.TestRailCase;
import org.testng.annotations.Test;

/**
 *
 * @author nullin
 */
public class TestClassB {

    @TestRailCase(automationId = "testB1")
    @Test
    public void test1() {
        // do nothing always passes
    }

}
