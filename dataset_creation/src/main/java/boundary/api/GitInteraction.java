package boundary.api;

import bean.ProjectInfoBean;
import bean.ReleaseBean;
import exception.GitException;
import org.eclipse.jgit.api.Git;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GitInteraction {

    private static Git git;
    private static final String GITHUB_API_BASE = "https://api.github.com/repos/";
    private static final int    PAGE_SIZE       = 100;

    private static String fetchJson(String url) throws GitException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            throw new GitException(e.getMessage());
        }
        if (response.statusCode() != 200) {
            throw new GitException("HTTP Error " + response.statusCode() + " — URL: " + url);
        }
        return response.body();
    }

    // Returns all release versions from Git tags, avoiding the "syncope-" prefix to not create conflict
    public static List<ReleaseBean> getAllReleases(ProjectInfoBean info) throws GitException {

        String repoOwner = info.getProjectOwner();
        String repoName  = info.getProjectRepo();
        String projectName = info.getProjectName();
        projectName = projectName.toLowerCase();

        List<ReleaseBean> releases = new ArrayList<>();
        int page = 1;

        while (true) {
            String url = GITHUB_API_BASE + repoOwner + "/" + repoName
                    + "/tags?per_page=" + PAGE_SIZE + "&page=" + page;
            String json = fetchJson(url);
            JSONArray tags = new JSONArray(json);

            if (tags.isEmpty()) break;

            for (int i = 0; i < tags.length(); i++) {
                JSONObject tag = tags.getJSONObject(i);
                String tagName = tag.getString("name");

                // Keep only tags that look like release tags (es. "syncope-3.0.0")
                if (!tagName.startsWith(projectName+"-")) continue;

                // Strip the prefix to get the version name (es. "3.0.0")
                String version = tagName.replace(projectName+"-", "");

                releases.add(new ReleaseBean(version));
            }

            page++;
        }

        return releases;
    }



    public static void doCheckout(ProjectInfoBean info) throws GitException {
        String repoPath = info.getLocalPath();
        String tagName = info.getTag();

        List<List<String>> commands = new ArrayList<>();
        commands.add(List.of("git", "reset", "--hard", "HEAD"));
        commands.add(List.of("git", "clean", "-fd"));
        commands.add(List.of("git", "checkout", "master"));
        commands.add(List.of("git", "checkout", tagName));

        try {
            for (List<String> cmd : commands) {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.directory(new File(repoPath));
                pb.redirectErrorStream(true);

                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new GitException("Checkout failed on command: " + cmd + " error: " + exitCode);
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new GitException("Error during checkout: " + e.getMessage());
        }
    }

    private static void open(ProjectInfoBean info) throws GitException {
        String repoPath = info.getLocalPath();
        repoPath = repoPath + "/.git";
        if(git == null){
            File repoDir = new File(repoPath);
            try{git = Git.open(repoDir);} catch (IOException e) {
                throw new GitException(e.getMessage());
            }
        }
    }

    public static Git getGit(ProjectInfoBean info) throws GitException{
        if(git == null){
            open(info);
        }
        return git;
    }

    public static void close(){
        if(git != null){
            git.close();
        }
    }
}