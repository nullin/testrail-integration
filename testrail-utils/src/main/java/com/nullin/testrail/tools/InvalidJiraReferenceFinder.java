package com.nullin.testrail.tools;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.nullin.testrail.client.TestRailClient;
import com.nullin.testrail.dto.*;

import java.net.URI;
import java.util.*;

/**
 * Find and prints out JIRA bug status for bugs referenced in the 'Reference' field of test cases
 *
 * When using references to point to bugs that are causing test cases to fail, this tool is useful for finding
 * references to resolved or closed bugs.
 *
 * @author nullin
 */
public class InvalidJiraReferenceFinder {

    /**
     * currently takes 6 args:
     *
     * {testrail URL} {testrail user} {testrail passwd} {jira URL} {jira user} {jira passwd}
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        TestRailClient client = new TestRailClient(args[0], args[1], args[2]);
        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final URI jiraServerUri = new URI(args[3]);
        final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, args[4], args[5]);

        int projectId = 1;
        String suiteName = "master"; //TODO: make configurable

        List<Suite> suites = client.getSuites(projectId);

        int suiteId = -1;
        for (Suite suite : suites) {
            if (suite.name.equals(suiteName)) {
                suiteId = suite.id;
            }
        }

        List<Case> cases = client.getCases(projectId, suiteId, 0, null);
        for (Case _case : cases) {
            String refs = _case.refs;
            if (refs == null || refs.isEmpty()) {
                continue;
            }
            String[] refsArr = refs.split(",");
            for (String ref : refsArr) {
                ref = ref.trim();
                checkReference(restClient, _case, ref);
            }
        }

        System.exit(0);
    }

    private static void checkReference(JiraRestClient client, Case aCase, String ref) {
        Issue issue = client.getIssueClient().getIssue(ref).claim();
        Status status = issue.getStatus();
        System.out.println(String.format("Case %8d (%75s), %d, %10s, %12s", aCase.id, aCase.automationId,
                aCase.typeId, ref, status.getName()));
    }

}