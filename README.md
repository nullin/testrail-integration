testrail-integration
====================

A prototype for connecting to TestRails and reporting results for automated tests executed using TestNG

Requirements/Assumptions
---------------------------------

* TestRail project should be setup as a project with single suite with (or without) baseline support.
* A custom field named `automation_id` should have been added to the Case type for the project.
* Annotations need to be added to test cases in Java code. See `TestRailCase` annotation
* Automation IDs need to be added as first parameter of the data-driven tests
* Automation IDs need to be unique within a TestRail test suite
* All test suite names should be unique

Workflow
--------

- Connect and get a list of all test suites in the project and cache in a map
- Also, get the list of all tests cases within the suites and create a map of the automation IDs to case IDs
- If same automation id is associated with multiple case ids in same suite, raise error and quit
- Create a new test plan (if an id isn't already specified) and associated the specified test suites
- For each result to be reported, get the method and it's enclosing class
	- get the annotations
	- Get the test run id
		(multiple runs for same suite, not supported yet. Need to handle configurationIds for that to work)
	- if the test was DD, the first parameter should be automation id
	- from automation id, get the case id
	- now we have case id and test run id
	- report the result
- Done with all results, close the test plan (by default or control based on user specified parameter)
- Finish
- Extras:
	- support creating test runs with configurations
	- output tests that were not reported into file w/ reason for failure

Different Scenarios
-------------------

1. Multiple suites (master and it's baselines) with same test cases

  We need to ensure that we have a way to get from automation id of a test case to a test result. This will be
  done by ensuring that we are picking up the automation id of the test cases from the correct suite. Suite names
  will have to be passed in as an argument.

2. Multiple TestRail test cases implemented as part of a single automated test

  We will need to ensure that the class doing the actual test result logging is available as a Singleton and that
  calls can be made from within the automated test cases as well to log results. Note that this means we aren't using
  the TestNG listener for such tests.

3. Automated data-driven tests

  We assume that the first parameter passed into a data-driven test case would be it's automation id and use this
  to figure out the associated TestRail test case.

4. Existing TestRail test run

  We will assume that the test run has all the required suites already associated with it. For this scenario,
  any specified suite names configuration will be ignored.

5. Non-existent TestRail test run

  We will create a new TestRail test run and also associate the TestRail test suites which were passed in to us
  while configuring the listener. For this scenario, suite names is a required configuration.