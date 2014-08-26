package com.nullin.testrail.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;


/**
 * TODO: javadocs
 *
 * @author nullin
 */
public class APIClient {
    private HttpClient httpClient;
    private String url;

    public APIClient(String url, String user, String password) {
        try {
            List<Header> headerList = new ArrayList<Header>();
            headerList.add(new BasicHeader("Content-Type", "application/json"));
            headerList.add(new BasicHeader("Authorization", "Basic " + getAuthorization(user, password)));

            httpClient = HttpClientBuilder.create().setDefaultHeaders(headerList).build();
            this.url = url + "/index.php?/api/v2/";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getAuthorization(String user, String password)
    {
        try
        {
            return getBase64((user + ":" + password).getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            // Not thrown
        }

        return "";
    }

    private String getBase64(byte[] buffer)
    {
        final char[] map = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '+', '/'
        };

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buffer.length; i++)
        {
            byte b0 = buffer[i++], b1 = 0, b2 = 0;

            int bytes = 3;
            if (i < buffer.length)
            {
                b1 = buffer[i++];
                if (i < buffer.length)
                {
                    b2 = buffer[i];
                }
                else
                {
                    bytes = 2;
                }
            }
            else
            {
                bytes = 1;
            }

            int total = (b0 << 16) | (b1 << 8) | b2;

            switch (bytes)
            {
                case 3:
                    sb.append(map[(total >> 18) & 0x3f]);
                    sb.append(map[(total >> 12) & 0x3f]);
                    sb.append(map[(total >> 6) & 0x3f]);
                    sb.append(map[total & 0x3f]);
                    break;

                case 2:
                    sb.append(map[(total >> 18) & 0x3f]);
                    sb.append(map[(total >> 12) & 0x3f]);
                    sb.append(map[(total >> 6) & 0x3f]);
                    sb.append('=');
                    break;

                case 1:
                    sb.append(map[(total >> 18) & 0x3f]);
                    sb.append(map[(total >> 12) & 0x3f]);
                    sb.append('=');
                    sb.append('=');
                    break;
            }
        }

        return sb.toString();
    }

    public String invokeHttpGet(String uriSuffix) throws IOException, ClientException {
        System.out.println("Invoking " + uriSuffix);
        HttpGet httpGet = new HttpGet(url + uriSuffix);
        HttpResponse response = httpClient.execute(httpGet);

        return consumeResponse(response);
    }

    public String invokeHttpPost(String uriSuffix, String jsonData) throws IOException, ClientException {
        System.out.println("Invoking " + uriSuffix + " with jsonData " + jsonData);
        HttpPost httpPost = new HttpPost(url + uriSuffix);
        StringEntity reqEntity = new StringEntity(jsonData);
        httpPost.setEntity(reqEntity);

        HttpResponse response = httpClient.execute(httpPost);

        return consumeResponse(response);
    }

    public String consumeResponse(HttpResponse response) throws ClientException, IOException {
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        entity.writeTo(os);
        String content = os.toString("UTF-8");

        //TODO: remove. here for debug
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(response.getStatusLine().toString());
        System.out.println(content);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        if (status != 200) {
            throw new ClientException("Received status code " + status + " with content '" + content + "'");
        }

        return content;
    }
/*
    public int getProjectId(String projectName) throws IOException, ElementNotFoundException {
        String body = httpGet("/index.php?/api/v2/get_projects").getBody();
        Project[] projects = this.objectMapper.readValue(body, Project[].class);
        for(int i = 0; i < projects.length; i++) {
            if (projects[i].getName().equals(projectName)) {
                return projects[i].getId();
            }
        }
        throw new ElementNotFoundException(projectName);
    }

    public int getSuiteId(int projectId, String suiteName) throws IOException, ElementNotFoundException {
        String body = httpGet("/index.php?/api/v2/get_suites/" + projectId).getBody();
        Suite[] suites = this.objectMapper.readValue(body, Suite[].class);
        for (int i = 0; i < suites.length; i++) {
            if (suites[i].getName().equals(suiteName)) {
                return suites[i].getId();
            }
        }
        throw new ElementNotFoundException(suiteName);
    }

    public String getCasesString(int projectId, int suiteId) {
        String result = "index.php?/api/v2/get_cases/" + projectId + "&suite_id=" + suiteId;
        return result;
    }

    public Case[] getCases(int projectId, int suiteId) throws IOException, ElementNotFoundException {
        // "/#{project_id}&suite_id=#{suite_id}#{section_string}"
        String body = httpGet("index.php?/api/v2/get_cases/" + projectId + "&suite_id=" + suiteId).getBody();
        return this.objectMapper.readValue(body, Case[].class);
    }

    public Section[] getSections(int projectId, int suiteId) throws IOException, ElementNotFoundException {
        String body = httpGet("index.php?/api/v2/get_sections/" + projectId + "&suite_id=" + suiteId).getBody();
        return this.objectMapper.readValue(body, Section[].class);
    }

    public Section addSection(String sectionName, int projectId, int suiteId) throws IOException, ElementNotFoundException {
        Section section = new Section();
        section.setName(sectionName);
        section.setSuiteId(suiteId);
        String payload = this.objectMapper.writeValueAsString(section);
        String body = httpPost("index.php?/api/v2/add_section/" + projectId , payload).getBody();
        return this.objectMapper.readValue(body, Section.class);
    }

    public Case addCase(String caseTitle, int sectionId) throws IOException {
        Case testcase = new Case();
        testcase.setTitle(caseTitle);
        String payload = this.objectMapper.writeValueAsString(testcase);
        String body = httpPost("index.php?/api/v2/add_case/" + sectionId, payload).getBody();
        return this.objectMapper.readValue(body, Case.class);
    }

    public TestRailResponse addResultsForCases(int runId, Results results) throws IOException {
        String payload = this.objectMapper.writeValueAsString(results);
        TestRailResponse response = httpPost("index.php?/api/v2/add_results_for_cases/" + runId, payload);
        return response;
    }

    public int addRun(int projectId, int suiteId, String description)
            throws JsonProcessingException, UnsupportedEncodingException, IOException {
        Run run = new Run();
        run.setSuiteId(suiteId);
        run.setDescription(description);
        String payload = this.objectMapper.writeValueAsString(run);
        String body = httpPost("index.php?/api/v2/add_run/" + projectId, payload).getBody();
        Run result = this.objectMapper.readValue(body, Run.class);
        return result.getId();
    }

    public boolean closeRun(int runId)
            throws JsonProcessingException, UnsupportedEncodingException, IOException {
        String payload = "";
        int status = httpPost("index.php?/api/v2/close_run/" + runId, payload).getStatus();
        return (200 == status);
    }
    */
}