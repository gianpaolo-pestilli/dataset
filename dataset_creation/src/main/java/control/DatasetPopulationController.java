package control;

import bean.ProjectInfoBean;
import boundary.api.GitInteraction;
import boundary.api.JiraInteraction;
import dao.DatasetDAO;
import dao.ReleaseDAO;
import dao.dto.ClassDTO;
import entity.Class;
import entity.ClassTracker;
import entity.Release;
import exception.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import settings.PropertiesSetter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DatasetPopulationController extends AppController {

    private List<Release> releases = new ArrayList<>();

    @Override
    public void start() throws ControllerException {
        try {
            String path = PropertiesSetter.getProjectLocalPath();
            ProjectInfoBean info = new ProjectInfoBean();
            info.setProjectName(PropertiesSetter.getProjectName());
            info.setLocalPath(path);

            Git git = GitInteraction.getGit(info);
            this.releases = ReleaseDAO.getVeryFirstReleases();
            List<ClassDTO> classes = DatasetDAO.getSonarClasses();

            List<ClassDTO> filtered = filter(classes);
            assemble(this.releases, filtered);

            addFeatures(git, info);

        } catch (PersistenceException | ConfigException | GitException e) {
            throw new ControllerException("Error occurred: " + e.getMessage());
        } finally {
            GitInteraction.close();
        }
    }

    @Override
    public void finish() throws ControllerException {
        try {
            DatasetDAO.writeDataset(this.releases);
        } catch (PersistenceException e) {
            throw new ControllerException(e.getMessage());
        }
    }

    private List<ClassDTO> filter(List<ClassDTO> classes) {
        List<ClassDTO> filtered = new ArrayList<>();
        for (ClassDTO c : classes) {
            String filePath = c.getPath().toLowerCase();
            if (filePath.endsWith(".java") && !filePath.contains("/test/")) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    private void assemble(List<Release> releases, List<ClassDTO> classes) {
        for (Release rel : releases) {
            for (ClassDTO cls : classes) {
                if (cls.getReleaseNumber() == rel.getProgressiveNumber()) {
                    rel.addClass(cls.getPath(), cls.getNumSmells(), cls.getNumMethods(), cls.getLoc());
                }
            }
        }
    }

    private void addFeatures(Git git, ProjectInfoBean info) throws ControllerException {
        Repository repo = git.getRepository();
        Set<String> bugFixTickets;
        try {
            bugFixTickets = new HashSet<>(JiraInteraction.getBugFixIds(info));
        } catch (JiraException e) {
            throw new ControllerException("Errore Jira: " + e.getMessage());
        }

        if (this.releases.isEmpty()) return;

        LocalDate lastReleaseDate = this.releases.get(this.releases.size() - 1).getReleaseDate();
        Map<String, ClassTracker> globalTrackers = new HashMap<>();

        try (RevWalk walk = new RevWalk(repo)) {
            walk.markStart(walk.parseCommit(repo.resolve("master")));
            walk.sort(RevSort.REVERSE);

            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(repo);
            df.setDetectRenames(true);

            LocalDate projectStartDate = null;

            for (RevCommit commit : walk) {
                LocalDate commitDate = commit.getAuthorIdent().getWhen().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                // Essendo la camminata in REVERSE, il primo commit è l'inizio assoluto del progetto
                if (projectStartDate == null) {
                    projectStartDate = commitDate;
                }

                if (commitDate.isAfter(lastReleaseDate)) continue;

                boolean isFix = bugFixTickets.stream().anyMatch(commit.getFullMessage()::contains);
                String authorEmail = commit.getAuthorIdent().getEmailAddress();

                processCommitDiffs(df, commit, isFix, authorEmail, commitDate, projectStartDate, globalTrackers);
            }

            calculateReleaseAges(projectStartDate);

        } catch (IOException e) {
            throw new ControllerException("Errore Git: " + e.getMessage());
        }

        propagateHistory();
    }

    // --- METODI PRIVATI ESTRATTI PER ABBATTERE LA COGNITIVE COMPLEXITY DI SonarQube ---

    private void processCommitDiffs(DiffFormatter df, RevCommit commit, boolean isFix, String authorEmail, LocalDate commitDate, LocalDate projectStartDate, Map<String, ClassTracker> globalTrackers) throws IOException {
        List<DiffEntry> diffs = df.scan(commit.getParentCount() > 0 ? commit.getParent(0).getTree() : null, commit.getTree());
        Release currentRelease = getReleaseForCommit(commitDate);

        for (DiffEntry diff : diffs) {
            String path = diff.getNewPath();
            if (path == null || !path.endsWith(".java") || path.toLowerCase().contains("/test/")) continue;

            int[] churn = calculateChurn(df, diff);
            int added = churn[0];
            int deleted = churn[1];

            updateClassFeatures(path, added, deleted, diffs.size(), isFix, authorEmail, commitDate, projectStartDate, globalTrackers, currentRelease);
        }
    }

    private int[] calculateChurn(DiffFormatter df, DiffEntry diff) throws IOException {
        int added = 0;
        int deleted = 0;
        for (HunkHeader hunk : df.toFileHeader(diff).getHunks()) {
            for (Edit edit : hunk.toEditList()) {
                if (edit.getType() == Edit.Type.INSERT) added += edit.getEndB() - edit.getBeginB();
                if (edit.getType() == Edit.Type.DELETE) deleted += edit.getEndA() - edit.getBeginA();
                if (edit.getType() == Edit.Type.REPLACE) {
                    deleted += edit.getEndA() - edit.getBeginA();
                    added += edit.getEndB() - edit.getBeginB();
                }
            }
        }
        return new int[]{added, deleted};
    }

    private void updateClassFeatures(String path, int added, int deleted, int diffSize, boolean isFix, String authorEmail, LocalDate commitDate, LocalDate projectStartDate, Map<String, ClassTracker> globalTrackers, Release currentRelease) {
        ClassTracker tracker = globalTrackers.computeIfAbsent(path, k -> new ClassTracker());
        tracker.incrementNumRev();
        if (isFix) tracker.incrementNumFix();
        tracker.addAuthor(authorEmail);
        tracker.addChurn(added, deleted);
        tracker.addChangeSet(diffSize);

        if (currentRelease != null) {
            for (Class cls : currentRelease.getClasses()) {
                if (isSameClass(cls.getName(), path)) {
                    cls.updateFromTracker(tracker);
                    cls.processCommitInWindow(added, deleted, diffSize, isFix, authorEmail, commitDate, projectStartDate);
                    break;
                }
            }
        }
    }

    private void calculateReleaseAges(LocalDate projectStartDate) {
        for (Release rel : this.releases) {
            if (projectStartDate != null && rel.getReleaseDate() != null) {
                long weeks = ChronoUnit.WEEKS.between(projectStartDate, rel.getReleaseDate());
                rel.setAge(Math.max(0, weeks));
            }
        }
    }

    private void propagateHistory() {
        for (int i = 1; i < this.releases.size(); i++) {
            Release prevRel = this.releases.get(i - 1);
            Release currRel = this.releases.get(i);

            for (Class currCls : currRel.getClasses()) {
                for (Class prevCls : prevRel.getClasses()) {
                    if (isSameClass(currCls.getName(), prevCls.getName())) {
                        currCls.setNumRevisionsFromBegin(Math.max(currCls.getNumRevisionsFromBegin(), prevCls.getNumRevisionsFromBegin()));
                        currCls.setNumFixesFromBegin(Math.max(currCls.getNumFixesFromBegin(), prevCls.getNumFixesFromBegin()));
                        currCls.setNumAuthorsFromBegin(Math.max(currCls.getNumAuthorsFromBegin(), prevCls.getNumAuthorsFromBegin()));
                        currCls.setChurnFromBegin(Math.max(currCls.getChurnFromBegin(), prevCls.getChurnFromBegin()));
                        currCls.setMaxLOCAddedFromBegin(Math.max(currCls.getMaxLOCAddedFromBegin(), prevCls.getMaxLOCAddedFromBegin()));
                        currCls.setAvgLOCAddedFromBegin(Math.max(currCls.getAvgLOCAddedFromBegin(), prevCls.getAvgLOCAddedFromBegin()));
                        currCls.setAvgChangeSetFromBegin(Math.max(currCls.getAvgChangeSetFromBegin(), prevCls.getAvgChangeSetFromBegin()));
                        currCls.setMaxChangeSetFromBegin(Math.max(currCls.getMaxChangeSetFromBegin(), prevCls.getMaxChangeSetFromBegin()));
                        break;
                    }
                }
            }
        }
    }

    // --- FINE METODI ESTRATTI ---

    private Release getReleaseForCommit(LocalDate commitDate) {
        for (Release rel : this.releases) {
            if (!commitDate.isAfter(rel.getReleaseDate())) return rel;
        }
        return null;
    }

    private boolean isSameClass(String sonarPath, String gitPath) {
        if (sonarPath == null || gitPath == null) return false;
        // Estrarre solo il nome del file (es: UserDAOImpl.java) è il modo più affidabile
        // per mappare le classi quando cambiano cartelle nel tempo.
        String name1 = sonarPath.substring(sonarPath.lastIndexOf('/') + 1);
        String name2 = gitPath.substring(gitPath.lastIndexOf('/') + 1);
        return name1.equals(name2);
    }
}