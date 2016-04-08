package com.nullin.testrail.util;

import org.testng.ITestResult;
import com.google.common.collect.Lists;

/**
 * An example of TestRail listener extended to take screenshots and pass this information to.
 *
 * @author nullin
 */
public class TestRailScreenshotListener extends com.nullin.testrail.TestRailListener {

    public String getScreenshotUrl(ITestResult result) {
        if (!Lists.newArrayList(ITestResult.FAILURE,
                ITestResult.SUCCESS_PERCENTAGE_FAILURE).contains(result.getStatus())) {
            //if the result doesn't represent a failure, no need to take screenshots
            return null;
        }

        try {
          //  Do stuff to take screen shot
          //  WebDriver driver = TWebDriverHolder.getWebDriver();
          //  String base64 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
          //  byte[] decodedScreenshot = Base64.decodeBase64(base64.getBytes());

            String testname = getTestName(result);
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = timestamp + "_" + testname + ".png";

            return "http://some.hostname.com/" + fileName;
        } catch (Exception e) {
            //do nothing and only log the exception here
        }
        return null;
    }

    private String getTestName(ITestResult result) {
        String testName = result.getTestClass().getRealClass().getName() + "." + result.getMethod().getMethodName();
        if (result.getParameters() != null && result.getParameters().length > 0) {
            testName += result.getParameters()[0];
        }

        return testName;
    }

}
