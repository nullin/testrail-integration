package com.nullin.testrail;

/**
 * TODO: add javadocs!
 *
 * @author nullin
 */
public class TestRailListenerArgs {

    private Boolean enabled;
    private Integer projectId;
    private Integer testPlanId;
    private String url;
    private String username;
    private String password;

    private TestRailListenerArgs() {}

    public static TestRailListenerArgs getNewTestRailListenerArgs() {
        TestRailListenerArgs args = new TestRailListenerArgs();
        args.enabled = Boolean.valueOf(System.getProperty("testRail.enabled"));
        String projectId = System.getProperty("testRail.projectId");
        if (projectId == null) {
            throw new IllegalArgumentException("TestRail Project ID not specified");
        } else {
            try {
                args.projectId = Integer.valueOf(projectId);
            } catch(NumberFormatException ex) {
                throw new IllegalArgumentException("Project Id is not an integer as expected");
            }
        }

        String planId = System.getProperty("testRail.testPlanId");
        if (planId != null) {
            try {
                args.testPlanId = Integer.valueOf(planId);
            } catch(NumberFormatException ex) {
                throw new IllegalArgumentException("Plan Id is not an integer as expected");
            }
        }

        if ((args.url = System.getProperty("testRail.url")) == null) {
            throw new IllegalArgumentException("TestRail URL not specified (testRail.url)");
        }

        if ((args.username = System.getProperty("testRail.username")) == null) {
            throw new IllegalArgumentException("TestRail user not specified (testRail.username)");
        }

        if ((args.password = System.getProperty("testRail.password")) == null) {
            throw new IllegalArgumentException("TestRail password not specified (testRail.password)");
        }

        return args;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public Integer getTestPlanId() {
        return testPlanId;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
