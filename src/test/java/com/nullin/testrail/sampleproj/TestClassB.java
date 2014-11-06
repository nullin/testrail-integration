package com.nullin.testrail.sampleproj;

import com.nullin.testrail.annotations.TestRailCase;
import org.testng.annotations.Test;

/**
 * TODO: add javadocs!
 *
 * @author nullin
 */
public class TestClassB {

    @TestRailCase(automationId = "34")
    @Test
    public void test1() {
        // do nothing always passes
    }

}
