package boundary.api;

import bean.ProjectInfoBean;
import bean.ReleaseBean;
import bean.TicketBean;
import exception.JiraException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JiraInteraction {

    private static final String JIRA_API_BASE = "https://issues.apache.org/jira/rest/api/2/project/";

    private static final String FIELD_VERSIONS = "versions";

    private JiraInteraction(){
        // Making it private
    }

    private static String fetchJson(String url) throws JiraException {

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new JiraException("HTTP error " + response.statusCode() + " — URL: " + url);
            }
            return response.body();
        } catch (IOException e) {
            throw new JiraException("I/O error during JIRA request: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JiraException("JIRA request has been interrupted: " + e.getMessage());
        }
    }

    public static List<ReleaseBean> getAllReleases(ProjectInfoBean info) throws JiraException {
        // Ignores the releases with no date and that have not been released yet (they might be changed, pointless studying them)
        String projectName = info.getProjectName();
        String url = JIRA_API_BASE + projectName;
        String json = fetchJson(url);

        JSONObject root = new JSONObject(json);
        JSONArray versions = root.getJSONArray(FIELD_VERSIONS);

        List<ReleaseBean> releases = new ArrayList<>();

        for (int i = 0; i < versions.length(); i++) {
            JSONObject v = versions.getJSONObject(i);


            if (!v.has("releaseDate") || !v.optBoolean("released", false)) continue;

            String id      = v.optString("id", "");
            String name    = v.optString("name", "");
            LocalDate date = LocalDate.parse(v.getString("releaseDate"));

            releases.add(new ReleaseBean(projectName, date, id, name));
        }

        // Order by date ascending
        releases.sort(Comparator.comparing(ReleaseBean::getReleaseDate));

        return releases;
    }

    public static Set<String> getBugFixIds(ProjectInfoBean info) throws JiraException {

        String projectName = info.getProjectName();
        projectName = projectName.toUpperCase();
        Set<String> bugFixIds = new HashSet<>();
        int startAt = 0;
        int maxResults = 1000;
        int total = 0;

        String jql = String.format(
                "project=\"%s\" AND issueType=\"Bug\" AND (status=\"closed\" OR status=\"resolved\") AND resolution=\"fixed\"",
                projectName
        );
        String encodedJql = URLEncoder.encode(jql, StandardCharsets.UTF_8);

        do {
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=" + encodedJql +
                    "&fields=key&startAt=" + startAt + "&maxResults=" + maxResults;

            String jsonResponse = fetchJson(url);
            JSONObject json = new JSONObject(jsonResponse);

            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");

            for (int i = 0; i < issues.length(); i++) {
                bugFixIds.add(issues.getJSONObject(i).getString("key"));
            }
            startAt += maxResults;

        } while (startAt < total);
        return bugFixIds;
    }

    public static List<TicketBean> getAllTickets(ProjectInfoBean info) throws JiraException {
        String projectName = info.getProjectName().toUpperCase();
        List<TicketBean> tickets = new ArrayList<>();

        int startAt = 0;
        int maxResults = 1000;
        int total = 0;


        String jql = String.format(
                "project=\"%s\" AND issueType=\"Bug\" AND (status=\"closed\" OR status=\"resolved\") AND resolution=\"fixed\"",
                projectName
        );
        String encodedJql = URLEncoder.encode(jql, StandardCharsets.UTF_8);

        do {

            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=" + encodedJql +
                    "&fields=key,created,versions&startAt=" + startAt + "&maxResults=" + maxResults;

            String jsonResponse = fetchJson(url);
            JSONObject json = new JSONObject(jsonResponse);

            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");

            for (int i = 0; i < issues.length(); i++) {
                JSONObject issue = issues.getJSONObject(i);


                String key = issue.getString("key");

                JSONObject fields = issue.getJSONObject("fields");


                String createdStr = fields.getString("created");

                LocalDate creationDate = LocalDate.parse(createdStr.substring(0, 10));


                List<String> affectedVersionsIds = new ArrayList<>();

                if (fields.has(FIELD_VERSIONS)) {
                    JSONArray versionsArray = fields.getJSONArray(FIELD_VERSIONS);
                    for (int j = 0; j < versionsArray.length(); j++) {
                        JSONObject versionObj = versionsArray.getJSONObject(j);
                        affectedVersionsIds.add(versionObj.getString("id"));
                    }
                }


                tickets.add(new TicketBean(key, creationDate, affectedVersionsIds));
            }

            startAt += maxResults;

        } while (startAt < total);

        return tickets;
    }
}