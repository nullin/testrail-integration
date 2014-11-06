package com.nullin.testrail.extra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.nullin.testrail.client.TestRailClient;
import com.nullin.testrail.dto.Section;
import com.nullin.testrail.dto.Suite;

/**
 * Simple class to generate some different kinds of projects with ~10000
 * test cases. Used it for testing out some different scenarios while evaluating
 * TestRail
 *
 * @author nullin
 */
public class GenerateTestCases {

    private TestRailClient client;

    public GenerateTestCases(String[] args) {
        client = new TestRailClient(args[0], args[1], args[2]);
    }

    public static void main(String[] args) throws Exception {
        GenerateTestCases tcs = new GenerateTestCases(args);
        tcs.generateType1();
        tcs.generateType2();
        tcs.generateType3();
        tcs.generateType4();
    }

    private void generateType1() throws Exception {
        List<Integer> sizes = new ArrayList<Integer>();
        sizes.add(100);
        sizes.add(50);
        sizes.add(25);
        sizes.add(25);

        int projectId = 8;
        for (int i = 0 ; i < 50 ; i++) {
            Section section = client.addSection(projectId, "Section " + UUID.randomUUID(), 0, 0);
            Collections.shuffle(sizes);
            for (int j = 0 ; j < 4 ; j++) {
                Section childSection = client.addSection(projectId, "Section " + UUID.randomUUID(), section.id, 0);
                for (int k = 0 ; k < sizes.get(j) ; k++) {
                    addCase(client, childSection.id);
                }
            }
        }
    }

    private void generateType2() throws Exception {
        List<Integer> sizes = new ArrayList<Integer>();
        sizes.add(100);
        sizes.add(50);
        sizes.add(25);
        sizes.add(25);

        int projectId = 9;
        int suiteId = 13;
        for (int i = 0 ; i < 50 ; i++) {
            Section section = client.addSection(projectId, "Section " + UUID.randomUUID(), 0, suiteId);
            Collections.shuffle(sizes);
            for (int j = 0 ; j < 4 ; j++) {
                Section childSection = client.addSection(projectId, "Section " + UUID.randomUUID(), section.id, suiteId);
                for (int k = 0 ; k < sizes.get(j) ; k++) {
                    addCase(client, childSection.id);
                }
            }
        }
    }

    private void generateType3() throws Exception {
        List<Integer> sizes = new ArrayList<Integer>();
        sizes.add(100);
        sizes.add(50);
        sizes.add(25);
        sizes.add(25);

        int projectId = 11;
        int suiteId = 15;
        for (int i = 0 ; i < 5 ; i++) {
            Section section = client.addSection(projectId, "Section " + UUID.randomUUID(), 0, suiteId);
            Collections.shuffle(sizes);
            for (int j = 0 ; j < 4 ; j++) {
                Section childSection = client.addSection(projectId, "Section " + UUID.randomUUID(), section.id, suiteId);
                for (int k = 0 ; k < sizes.get(j) ; k++) {
                    addCase(client, childSection.id);
                }
            }
        }
    }

    private void generateType4() throws Exception {
        List<Integer> sizes = new ArrayList<Integer>();
        sizes.add(100);
        sizes.add(50);
        sizes.add(25);
        sizes.add(25);

        int projectId = 12;
        for (int i = 0 ; i < 6 ; i++) {
            Suite suite = client.addSuite(projectId, "Suite " + UUID.randomUUID());
            Collections.shuffle(sizes);
            for (int j = 0 ; j < 4 ; j++) {
                Section childSection = client.addSection(projectId, "Section " + UUID.randomUUID(), 0, suite.id);
                for (int k = 0 ; k < sizes.get(j) ; k++) {
                    addCase(client, childSection.id);
                }
            }
        }
    }

    private void addCase(TestRailClient client, int sectionId) throws Exception {
        Map<String, String> fields = new HashMap<String, String>();
        fields.put("custom_expected", "Expected\n\n" + getString(20));
        fields.put("custom_preconds", "Preconds\n\n" + getString(5));
        fields.put("custom_steps", "Steps\n\n" + getString(10));
        client.addCase(sectionId, "Case " + UUID.randomUUID(), fields);
    }

    private String getString(int i) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0 ; c < i ; c++) {
            sb.append(UUID.randomUUID().toString());
            if (c % (new Random().nextInt(i) + 1) == 0) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

}
