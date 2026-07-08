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

    // Costante definita per risolvere lo smell di SonarQube sulle stringhe duplicate
    private static final String FIELD_VERSIONS = "versions";

    private JiraInteraction(){
        // Making it private
    }

    private static String fetchJson(String url) throws JiraException {
        // Implementazione del try-with-resources per chiudere in automatico HttpClient
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
        // Utilizzo della costante al posto della stringa letterale
        JSONArray versions = root.getJSONArray(FIELD_VERSIONS);

        List<ReleaseBean> releases = new ArrayList<>();

        for (int i = 0; i < versions.length(); i++) {
            JSONObject v = versions.getJSONObject(i);

            // Skip releases without date or not yet released
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

        // Query JQL blindata
        String jql = String.format(
                "project=\"%s\" AND issueType=\"Bug\" AND (status=\"closed\" OR status=\"resolved\") AND resolution=\"fixed\"",
                projectName
        );
        String encodedJql = URLEncoder.encode(jql, StandardCharsets.UTF_8);

        do {
            // Ottimizzazione: scarichiamo solo key, created e versions
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=" + encodedJql +
                    "&fields=key,created,versions&startAt=" + startAt + "&maxResults=" + maxResults;

            String jsonResponse = fetchJson(url);
            JSONObject json = new JSONObject(jsonResponse);

            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");

            for (int i = 0; i < issues.length(); i++) {
                JSONObject issue = issues.getJSONObject(i);

                // 1. ID del Ticket (es. SYNCOPE-123)
                String key = issue.getString("key");

                JSONObject fields = issue.getJSONObject("fields");

                // 2. Data di Creazione (Opening Version)
                String createdStr = fields.getString("created");
                // Tronchiamo la stringa ISO 8601 per prendere solo YYYY-MM-DD
                LocalDate creationDate = LocalDate.parse(createdStr.substring(0, 10));

                // 3. ID interni di Jira delle Affected Versions (Injected Versions)
                List<String> affectedVersionsIds = new ArrayList<>();
                // Utilizzo della costante al posto della stringa letterale
                if (fields.has(FIELD_VERSIONS)) {
                    JSONArray versionsArray = fields.getJSONArray(FIELD_VERSIONS);
                    for (int j = 0; j < versionsArray.length(); j++) {
                        JSONObject versionObj = versionsArray.getJSONObject(j);
                        affectedVersionsIds.add(versionObj.getString("id"));
                    }
                }

                // Costruiamo e aggiungiamo il Bean alla lista
                tickets.add(new TicketBean(key, creationDate, affectedVersionsIds));
            }

            startAt += maxResults;

        } while (startAt < total);

        return tickets;
    }
}