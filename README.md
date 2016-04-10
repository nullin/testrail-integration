testrail-integration
====================

A library for connecting to TestRail and reporting results for automated tests executed using TestNG.

Requirements/Assumptions
------------------------

### TestRail Configuration

* Project should be setup as a single repository with baseline support.
* A custom string field named `automation_id` should be added to the `Case` fields for the project.
* ~~All suite names should be unique~~
* Automation IDs need to be unique within a TestRail test suite

### Test Code

* Annotations need to be added to test cases in Java code. See `TestRailCase` annotation
* Automation IDs need to be added as first parameter of the data-driven tests

### Other Assumptions

* TestRail Test Plan needs to be pre-created and associated with the required suite.
* TestRail Test Plan can contain multiple plan entries (runs), but each of that run is associated with the same Suite. e.g.
 this allows us to separate API and UI tests into two different entries with in the same Test Plan.
* If the same automation id appears twice for the same, result will only be reported once. This is an invalid configuration 
and should be fixed quickly.

How to use `automation_id`
---------------------

The custom string field `automation_id` in TestRail contains unique strings that are referenced by test code to report
results. This unique string is associated to tests in multiple ways:

### Sing Test Method

Simply add `TestRailCase` annotation to the test method and the listener will take care of reporting the result

```java
    
    @TestRailCase("testC1")
    @Test
    public void test1() {
        Assert.assertEquals(1, 1);
    }
```

In above example, a `PASS` will be reported for test with automation id `testC1`.

### Data-driven Test

No annotation needs to be added in this case. We assume that the first parameter passed into the data driven test is
the unique automation id for the data-driven tests.

```java
    
    @DataProvider(name = "DP")
    public Object[][] getData() {
        return new Object[][] {
                {"testA2", 10, 10},
                {"testA3", 10, 5}
        };
    }

    @Test(dataProvider = "DP")
    public void test2(String testId, int x, int y) {
        Assert.assertEquals(x, y);
    }
```

In above example, a `PASS` will be reported for test case with automation id `testA2` and a `FAILURE` for `testA3`.

### Test Method for Multiple TestRail Test Cases

By adding `TestRailCase` annotation with `selfReporting` set to true, we tell the `TestRailReporter` to skip this 
test when processing it as it's responsible for it's own reporting of results.

```java
    
    @TestRailCase(selfReporting = true)
    @Test
    public void test5() {

        //do something

        Map<String, Object> result = new HashMap<String, Object>();
        result.put(TestRailReporter.KEY_STATUS, ResultStatus.PASS);
        TestRailReporter.getInstance().reportResult("testC3", result);

        //do something 
        
        result.put(TestRailReporter.KEY_STATUS, ResultStatus.FAIL);
        result.put(TestRailReporter.KEY_THROWABLE, new IOException("Something very bad happened!!"));
        TestRailReporter.getInstance().reportResult("testC4", result);
    }
```

In above example, a `PASS` will be reported for test case with automation id `testC3` and a `FAILURE` for `testC4`. This
is a simplified example; in actual code, you might want to extend TestNG's `SoftAssert` and delegate the reporting to
that class.

Listeners and Reporters
-----------------------

`com.nullin.testrail.TestRailListener` implements `ITestListener` and `IConfigurationListener` interfaces from TestNG,
and is responsible for reporting results to TestRail. `TestRailListener` internally uses `com.nullin.testrail.TestRailReporter` 
to report results to TestRail. Test code can also directly use methods in `TestRailReporter` as shown in example above.

### Listener Configuration

Following system properties need to be configured during startup to configure/enable TestRail reporter

* `testRail.enabled` : boolean (true|false) value to enable/disable reporting to TestRail.
* `testRail.url` : Base URL to TestRail.
* `testRail.username` : User to use to authenticate to TestRail instance.
* `testRail.password` : Password to authenticate to TestRail instance.
* `testRail.testPlanId` : ID of a pre-created Test Plan in TestRail. This is an integer that you can get via APIs. Via, 
TestRail UI, this is the integer part of an ID that is shown next to a Test Plan. E.g. for `R3285` id displayed in UI, it will 
be `3285`.

### Listener Startup

During startup, we try and connect to TestRail and get the Test Plan using the user specified id. If any of this fails,
we disable the `TestRailReporter`, log this condition, but do not fail the test execution. 

Also, during startup, we create a cache of all Test Cases and associated `automation id`s. During this process, we log
all instances when we encounter the same `automation id` multiple times.

### Listener Logging

We use `java.util.logging`.


Extending `TestRailListener`
----------------------------

We have two methods that can be overridden by extending `TestRailListener`
 
1. `public String getScreenshotUrl(ITestResult result)` 
  Extend this method to return a URL for the captured screen shot for UI tests. If specified, we inline the image into TestRail result. 

2. `public Map<String, String> getMoreInformation(ITestResult result)` 
  Extend this method to provide more information about the test result or the test run in general. We have used this 
  method to specify information about environment variables, target environment etc. 
  
  __NOTE__: the test class/method/parameter information is automatically logged and doesn't need to be returned by this method.

Workflow
--------

```

- Connect and get a list of all test suites in the project and cache in a map
- Also, get the list of all tests cases within the suites and create a map of the automation IDs to case IDs
- If same automation id is associated with multiple case ids in same suite, raise error and quit
- Get the test plan based on the specified plan name or id
- For each result to be reported, get the method and it's enclosing class (multiple runs for same suite,
not supported yet. Need to handle configurationIds for that to work)
	- get the annotations and figure out the automation id
	- if the test was DD, the first parameter should be automation id
	- from automation id, get the case id
	- now we have case id and test run id
	- report the result
- Finish

```

Future work
--------------------

- Support creating test runs with configurations
- Output tests that were not reported into file w/ reason for failure
- Create a new test plan (if an id isn't already specified) and associated the specified test suites
- When done with all results, close the test plan (by default or based on user specified parameter)
