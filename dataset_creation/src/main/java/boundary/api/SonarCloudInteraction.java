package boundary.api;

import bean.ClassesBean;
import bean.ProjectInfoBean;
import exception.ConfigException;
import exception.SonarException;
import org.json.JSONArray;
import org.json.JSONObject;
import settings.PropertiesSetter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class SonarCloudInteraction {

    private static final String SONAR_API_BASE = "https://sonarcloud.io/api/measures/component_tree";
    private static final String METRIC_SMELLS  = "code_smells";
    private static final String METRIC_EFFORT  = "sqale_index";
    private static final String METRIC_TECHNICAL_DEBT_RATIO = "sqale_debt_ratio";
    private static final int    PAGE_SIZE      = 500;


    //  Calling the API...
    private static String fetchJson(String url) throws SonarException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response;
        try{
             response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            throw new SonarException(e.getMessage());
        }

        if (response.statusCode() != 200) {
            throw new SonarException("Errore HTTP " + response.statusCode() + " — URL: " + url);
        }
        return response.body();
    }

    // Building URL...
    private static String buildUrl(String projectKey, String metrics, int page) {
        return String.format(
                "%s?component=%s&metricKeys=%s&qualifiers=FIL&ps=%d&p=%d",
                SONAR_API_BASE, projectKey, metrics, PAGE_SIZE, page
        );
    }

    // Download all pages...
    private static List<JSONObject> fetchAllComponents(String projectKey, String metrics) throws SonarException{
        List<JSONObject> allComponents = new ArrayList<>();
        int page = 1;
        int total;

        do {
            String json = fetchJson(buildUrl(projectKey, metrics, page));
            JSONObject root = new JSONObject(json);

            total = root.getJSONObject("paging").getInt("total");
            JSONArray components = root.getJSONArray("components");

            for (int i = 0; i < components.length(); i++) {
                allComponents.add(components.getJSONObject(i));
            }

            page++;
        } while ((long)(page - 1) * PAGE_SIZE < total);

        return allComponents;
    }

    // Avoiding test classes...
    private static boolean isTestClass(String path) {
        return path.contains("/test/") || path.contains("src/test/");
    }

    // Reading the integer metric value...
    private static int extractMetricValue(JSONObject component, String metricName) {
        JSONArray measures = component.optJSONArray("measures");
        if (measures == null) return 0;

        for (int i = 0; i < measures.length(); i++) {
            JSONObject measure = measures.getJSONObject(i);
            if (metricName.equals(measure.getString("metric"))) {
                return Integer.parseInt(measure.optString("value", "0"));
            }
        }
        return 0;
    }

    // Reading the decimal metric value (for Technical debt ratio)...
    private static double extractDecimalMetricValue(JSONObject component, String metricName) {
        JSONArray measures = component.optJSONArray("measures");
        if (measures == null) return 0.0;

        for (int i = 0; i < measures.length(); i++) {
            JSONObject measure = measures.getJSONObject(i);
            if (metricName.equals(measure.getString("metric"))) {
                try {
                    return Double.parseDouble(measure.optString("value", "0.0"));
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        }
        return 0.0;
    }

    // Getting all the classes in any order...
    public static List<ClassesBean> getAllClasses(ProjectInfoBean info) throws SonarException {

        String projectKey = info.getProjectKey();
        String projectName = info.getProjectName();
        String releaseVersion = info.getReleaseVersion();

        List<ClassesBean> result = new ArrayList<>();

        List<JSONObject> components = fetchAllComponents(projectKey, METRIC_SMELLS + "," + METRIC_EFFORT + "," + METRIC_TECHNICAL_DEBT_RATIO);

        for (JSONObject component : components) {
            String path = component.optString("path", "");
            if (isTestClass(path)) continue;

            // Extract Technical debt ratio as an integer (multiplied by 100 for precision)
            double technicalDebtRatio = extractDecimalMetricValue(component, METRIC_TECHNICAL_DEBT_RATIO);

            ClassesBean c = new ClassesBean(projectName, path, releaseVersion,
                    extractMetricValue(component, METRIC_SMELLS),
                    extractMetricValue(component, METRIC_EFFORT),
                    technicalDebtRatio
            );
            result.add(c);
        }


        return result;
    }

    // It returns the Release VERSION (not the Jira ID) of the latest analyzed release
    public static String getCurrentReleaseVersion() throws SonarException {
        String projectKey;
        try {
            projectKey = PropertiesSetter.getSonarKey();
        } catch (ConfigException e) {
            throw new SonarException(e.getMessage());
        }

        String url = "https://sonarcloud.io/api/project_analyses/search?project=" + projectKey + "&ps=1";
        String json = fetchJson(url);

        JSONObject root = new JSONObject(json);
        JSONArray analyses = root.getJSONArray("analyses");

        if (analyses.isEmpty()) {
            throw new SonarException("No analyzed releases: " + projectKey);
        }

        String version = analyses.getJSONObject(0).optString("projectVersion", "");

        if (version.isBlank()) {
            throw new SonarException("Not available Project Version");
        }

        return version.replace("-SNAPSHOT", "").trim();
    }
}