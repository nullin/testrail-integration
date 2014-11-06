package com.nullin.testrail.sampleproj;

import java.util.Random;

import com.nullin.testrail.annotations.TestRailCase;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author nullin
 */
public class TestClassA {

    @TestRailCase(automationId = "testA1")
    @Test
    public void test1() {
        Assert.assertTrue(getResult(10, 2));
    }

    @DataProvider(name = "MYDP")
    public Object[][] getData() {
        return new Object[][] {
                {"testA2", 10, 3},
                {"testA3", 10, 5}
        };
    }

    @TestRailCase(dataDriven = true)
    @Test(dataProvider = "MYDP")
    public void test2(String testId, int x, int y) {
        Assert.assertTrue(getResult(x, y));
    }

    public static boolean getResult(int max, int r) {
        return new Random(max).nextInt() % r != 0;
    }

}
