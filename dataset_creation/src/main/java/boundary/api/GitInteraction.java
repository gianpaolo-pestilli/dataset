package boundary.api;

import bean.CommitBean;
import bean.ProjectInfoBean;
import bean.ReleaseBean;
import exception.GitException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitInteraction {

    private static Git git;
    private static final String GITHUB_API_BASE = "https://api.github.com/repos/";
    private static final int    PAGE_SIZE       = 100;

    private GitInteraction(){
        // Making it private
    }

    private static String fetchJson(String url) throws GitException {

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/vnd.github+json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new GitException("HTTP Error " + response.statusCode() + " — URL: " + url);
            }
            return response.body();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GitException("Richiesta interrotta: " + e.getMessage());
        } catch (IOException e) {
            throw new GitException("Errore di I/O: " + e.getMessage());
        }
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GitException("Checkout interrotto: " + e.getMessage());
        } catch (IOException e) {
            throw new GitException("Error during checkout: " + e.getMessage());
        }
    }

    private static void open(ProjectInfoBean info) throws GitException {
        String repoPath = info.getLocalPath();
        repoPath = repoPath + "/.git";
        if(git == null){
            File repoDir = new File(repoPath);
            try{
                git = Git.open(repoDir);
            } catch (IOException e) {
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

    public static Set<String> getBuggyMessageID(ProjectInfoBean info) throws GitException {
        Git currentGit = getGit(info);
        Set<String> foundTicketIds = new HashSet<>();

        String projectName = info.getProjectName().toUpperCase();
        Pattern pattern = Pattern.compile(projectName + "-\\d+");

        try {
            Iterable<RevCommit> commits = currentGit.log().all().call();

            for (RevCommit commit : commits) {
                String message = commit.getFullMessage();
                Matcher matcher = pattern.matcher(message);
                while (matcher.find()) {
                    foundTicketIds.add(matcher.group());
                }
            }
        } catch (GitAPIException | IOException e) {
            throw new GitException("Errore in Git: " + e.getMessage());
        }

        return foundTicketIds;
    }

    public static List<CommitBean> extractAllCommits(ProjectInfoBean info) throws GitException {
        Git currentGit = getGit(info);
        List<CommitBean> result = new ArrayList<>();

        String projectName = info.getProjectName().toUpperCase();
        Pattern pattern = Pattern.compile(projectName + "-\\d+");

        try {
            Repository repository = currentGit.getRepository();
            Iterable<RevCommit> commits = currentGit.log().all().call();

            try (DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                df.setRepository(repository);
                df.setDiffComparator(RawTextComparator.DEFAULT);
                df.setDetectRenames(true);

                for (RevCommit commit : commits) {
                    Matcher matcher = pattern.matcher(commit.getFullMessage());
                        if (!matcher.find()) {
                            continue;
                        }


                    LocalDate commitDate = LocalDate.ofInstant(
                            Instant.ofEpochSecond(commit.getCommitTime()),
                            ZoneId.systemDefault()
                    );


                    List<String> touchedClasses = getTouchedClasses(df, commit);


                    matcher.reset();
                    while (matcher.find()) {
                        String ticketId = matcher.group();
                        result.add(new CommitBean(ticketId, commitDate, touchedClasses));
                    }
                }
            }
        } catch (GitAPIException | IOException e) {
            throw new GitException("Errore durante l'estrazione dei commit: " + e.getMessage());
        }

        return result;
    }


    private static List<String> getTouchedClasses(DiffFormatter df, RevCommit commit) throws IOException {
        List<String> touchedClasses = new ArrayList<>();
        if (commit.getParentCount() > 0) {
            RevCommit parent = commit.getParent(0);
            List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());

            for (DiffEntry diff : diffs) {
                String newPath = diff.getNewPath();
                if (newPath.endsWith(".java") && !newPath.toLowerCase().contains("test")) {
                    touchedClasses.add(newPath);
                }
            }
        }
        return touchedClasses;
    }
}