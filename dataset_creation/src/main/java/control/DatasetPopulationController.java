package control;

import bean.MessageBean;
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
import org.eclipse.jgit.patch.FileHeader;
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

public class DatasetPopulationController extends AppController{

    private List<Release> releases = new ArrayList<>();

    @Override
    public void start() throws ControllerException {
        //Call this only if the SonarCloud information have been retrieved
        userBoundary.printMessage(new MessageBean("Starting filtering classes..."));
        // First of all we have to build releases
        try{
            String path = PropertiesSetter.getProjectLocalPath();
            ProjectInfoBean info = new ProjectInfoBean();
            info.setProjectName(PropertiesSetter.getProjectName());
            info.setLocalPath(path);
            Git git = GitInteraction.getGit(info);
            List<Release> releases = ReleaseDAO.getAllReleases();
            List<ClassDTO> classes = DatasetDAO.getSonarClasses();
            List<ClassDTO> filtered = filter(classes);
            assemble(releases,filtered);
            this.releases = releases;
            addFeatures(git,info);

        } catch (PersistenceException | ConfigException | GitException e) {
            throw new ControllerException("Error occurred while fetching releases: " + e.getMessage());
        }finally{
            GitInteraction.close();
        }

    }
    @Override
    public void finish() throws ControllerException {
        try{
            DatasetDAO.writeDataset(this.releases);

        } catch (PersistenceException e) {
            throw new ControllerException(e.getMessage());
        }
        userBoundary.printMessage(new MessageBean("--- Success ---"));
    }

    private List<ClassDTO> filter(List<ClassDTO> classes){
        List<ClassDTO> filtered = new ArrayList<>();
        for (ClassDTO c : classes) {

            String filePath = c.getPath().toLowerCase();
            boolean isJavaFile = filePath.endsWith(".java");
            boolean isTestClass = filePath.contains("/test/");
            if (isJavaFile && !isTestClass) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    private void assemble(List<Release> releases, List<ClassDTO> classes){
        // Complete the releases with provided classes
        for(Release rel: releases){
            for(ClassDTO cls : classes){
                if(cls.getReleaseNumber() == rel.getProgressiveNumber()){
                    rel.addClass(cls.getPath(),cls.getNumSmells(),cls.getNumMethods(),cls.getLoc());
                }
            }
        }
    }

    private void addFeatures(Git git, ProjectInfoBean info) throws ControllerException {
        Repository repo = git.getRepository();
        Set<String> bugFixTickets;
        try {
            // Estrae gli ID da Jira. Questi sono il nostro vocabolario per capire se un commit è un Fix
            bugFixTickets = new HashSet<>(JiraInteraction.getBugFixIds(info));
        } catch (JiraException e) {
            throw new ControllerException("Errore Jira: " + e.getMessage());
        }

        if (this.releases.isEmpty()) return;

        LocalDate lastReleaseDate = this.releases.get(this.releases.size() - 1).getReleaseDate();
        Map<String, ClassTracker> globalTrackers = new HashMap<>(); // Lo schedario di tutti i file
        LocalDate projectStartDate = null;

        // Inizializza il camminatore temporale di Git
        try (RevWalk walk = new RevWalk(repo)) {
            walk.markStart(walk.parseCommit(repo.resolve("master")));
            walk.sort(RevSort.REVERSE); // Ordine cronologico dal vecchio al nuovo

            // DiffFormatter è lo strumento che "legge" il codice aggiunto/rimosso
            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(repo);
            df.setDetectRenames(true);

            // INIZIO LOOP SUI COMMIT
            for (RevCommit commit : walk) {
                LocalDate commitDate = commit.getAuthorIdent().getWhen().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                // Identifica la data di nascita del progetto e pre-calcola le Età delle release (Metrica 18)
                if (projectStartDate == null) {
                    projectStartDate = commitDate;
                    for (int i = 0; i < this.releases.size(); i++) {
                        Release current = this.releases.get(i);
                        if (i == 0) {
                            current.setAgeInDays(Math.max(0, ChronoUnit.DAYS.between(projectStartDate, current.getReleaseDate())));
                        } else {
                            Release previous = this.releases.get(i - 1);
                            current.setAgeInDays(Math.max(0, ChronoUnit.DAYS.between(previous.getReleaseDate(), current.getReleaseDate())));
                        }
                    }
                }

                // Ottimizzazione: ferma l'analisi se andiamo oltre l'ultima release utile
                if (commitDate.isAfter(lastReleaseDate)) {
                    break;
                }

                // Identifichiamo a quale release appartiene questo commit in base alla data
                Release currentRelease = getReleaseForCommit(commitDate);

                // Controlliamo se il messaggio del commit contiene il ticket del bug (Serve per Metrica 4 e 5)
                String commitMessage = commit.getFullMessage();
                boolean isFix = bugFixTickets.stream().anyMatch(commitMessage::contains);
                String authorEmail = commit.getAuthorIdent().getEmailAddress();

                // Chiediamo a Git l'elenco dei file toccati (ChangeSet)
                List<DiffEntry> diffs = df.scan(
                        commit.getParentCount() > 0 ? commit.getParent(0).getTree() : null,
                        commit.getTree()
                );
                int changeSetSize = diffs.size(); // Questo è il numero per le metriche 14, 15, 16, 17

                // INIZIO LOOP SUI FILE MODIFICATI NEL COMMIT
                for (DiffEntry diff : diffs) {
                    String path = diff.getNewPath();

                    // Saltiamo le classi non Java e le classi di Test (non servono per i difetti)
                    if (path == null || !path.endsWith(".java") || path.toLowerCase().contains("/test/")) {
                        continue;
                    }

                    // --- IL MOMENTO DELL'ESTRAZIONE FISICA DELLE RIGHE ---
                    int added = 0;
                    int deleted = 0;
                    FileHeader fileHeader = df.toFileHeader(diff);
                    for (HunkHeader hunk : fileHeader.getHunks()) {
                        for (Edit edit : hunk.toEditList()) {
                            switch (edit.getType()) {
                                case INSERT: added += edit.getEndB() - edit.getBeginB(); break;
                                case DELETE: deleted += edit.getEndA() - edit.getBeginA(); break;
                                case REPLACE:
                                    deleted += edit.getEndA() - edit.getBeginA();
                                    added += edit.getEndB() - edit.getBeginB();
                                    break;
                                case EMPTY: break;
                            }
                        }
                    }

                    // --- IL MOMENTO DELL'ACCUMULO ---

                    // 1. Accumulo Storico (Tracker Globale)
                    ClassTracker tracker = globalTrackers.computeIfAbsent(path, k -> new ClassTracker());
                    tracker.incrementNumRev();
                    if (isFix) tracker.incrementNumFix();
                    tracker.addAuthor(authorEmail);
                    tracker.addChurn(added, deleted);
                    tracker.addChangeSet(changeSetSize);

                    // 2. Accumulo in Finestra (La singola classe della Release)
                    if (currentRelease != null) {
                        for (Class cls : currentRelease.getClasses()) {
                            // Se il file modificato corrisponde a una classe che stiamo tracciando...
                            if (cls.getName().equals(path)) {
                                // A. "Travasiamo" il contenitore storico dentro la classe
                                cls.updateFromTracker(tracker);

                                // B. Settiamo l'età generale della release
                                cls.setAgeOfRelease(currentRelease.getAgeInDays());

                                // C. Aggiorniamo le metriche limitate a QUESTA release (passandogli la releaseDate per la Weighted Age)
                                cls.processCommitInWindow(added, deleted, changeSetSize, isFix,
                                        authorEmail, commitDate, currentRelease.getReleaseDate());

                                break; // Abbiamo aggiornato la classe, inutile scorrere le altre
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ControllerException("Errore JGit: " + e.getMessage());
        }
    }

    private Release getReleaseForCommit(LocalDate commitDate) {
        for (Release rel : this.releases) {
            if (!commitDate.isAfter(rel.getReleaseDate())) {
                return rel;
            }
        }
        return null;
    }

}
