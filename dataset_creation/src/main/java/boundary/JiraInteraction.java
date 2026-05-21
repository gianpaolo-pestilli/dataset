//package boundary;
//
//import java.io.*;
//import java.net.URL;
//import java.nio.charset.Charset;
//
//import exception.ConfigException;
//import exception.JiraException;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import settings.PropertiesSetter;
//
//public class JiraInteraction {
//
//    private static String readAll(Reader rd) throws IOException {
//        StringBuilder sb = new StringBuilder();
//        int cp;
//        while ((cp = rd.read()) != -1) {
//            sb.append((char) cp);
//        }
//        return sb.toString();
//    }
//
//    public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
//        InputStream is = new URL(url).openStream();
//        try {
//            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
//            String jsonText = readAll(rd);
//            JSONArray json = new JSONArray(jsonText);
//            return json;
//        } finally {
//                is.close();
//            }
//    }
//
//
//    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
//        InputStream is = new URL(url).openStream();
//            try {
//                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
//                String jsonText = readAll(rd);
//                JSONObject json = new JSONObject(jsonText);
//                return json;
//            } finally {
//                is.close();
//            }
//    }
//
//
//    public static void printTicket() throws IOException, JSONException, JiraException {
//        String projName = null;
//        try {
//            projName = PropertiesSetter.getProjectName();
//        } catch (ConfigException e){
//            throw new JiraException(e.getMessage());
//        }
//
//        Integer j = 0, i = 0, total = 1;
//            //Get JSON API for closed bugs w/ AV in the project
//            do {
//                //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
//                j = i + 1000;
//                String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
//                        + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
//                        + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
//                        + i.toString() + "&maxResults=" + j.toString();
//                JSONObject json = readJsonFromUrl(url);
//                JSONArray issues = json.getJSONArray("issues");
//                total = json.getInt("total");
//                for (; i < total && i < j; i++) {
//                    //Iterate through each bug
//                    String key = issues.getJSONObject(i%1000).get("key").toString();
//
//                    // Still understanding where to store this information
//                    System.out.println(key);
//                }
//            } while (i < total);
//        }
//
//

package boundary;
import bean.ProjectInfoBean;
import bean.ReleaseBean;
import exception.JiraException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class JiraInteraction {

    private static final String JIRA_API_BASE = "https://issues.apache.org/jira/rest/api/2/project/";

    private static String fetchJson(String url) throws JiraException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        try {
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
            JSONArray versions = root.getJSONArray("versions");

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

    }