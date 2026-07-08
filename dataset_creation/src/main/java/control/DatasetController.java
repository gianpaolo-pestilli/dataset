package control;

import bean.MessageBean;
import bean.ProjectInfoBean;
import bean.ReleaseBean;
import boundary.api.GitInteraction;
import boundary.api.JiraInteraction;
import dao.ReleaseDAO;
import exception.*;
import settings.PropertiesSetter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*

The ranking of the classes has been done relying on SonarCloud and current release,
for the dataset we want something more accurate, so the first step is about filtering the releases
spotting the discrepancies between Jira and Git and eliminating branches different from the main one

*/

public class DatasetController extends AppController {

    // The following list contains the beans of the FILTERED releases
    private List<ReleaseBean> allReleases = new ArrayList<>();
    private List<ReleaseBean> firstReleases = new ArrayList<>();
    private AppController datasetBuilderController;


    @Override
    public void start() throws ControllerException {
        String projectName;
        String owner;
        String repo;
        String releaseFile;
        String firstReleaseFile;
        try {
            projectName = PropertiesSetter.getProjectName();
            owner = PropertiesSetter.getOwner();
            repo = PropertiesSetter.getRepo();
            releaseFile = ReleaseDAO.getAllReleasesFilename();
            firstReleaseFile = ReleaseDAO.getFirstReleasesFilename();

        } catch (ConfigException e) {
            throw new ControllerException("Error while retrieving the file: " + e.getMessage());
        }


        MessageBean mess;

        mess = new MessageBean("Analyzing the project: " + projectName + "...");
        userBoundary.printMessage(mess);

        mess = new MessageBean("\nFiltering releases...");
        userBoundary.printMessage(mess);
        // Start from here !!
        mess = new MessageBean("Considered releases will be written into this file: " + releaseFile);
        userBoundary.printMessage(mess);

        ProjectInfoBean param = new ProjectInfoBean(
                null,
                projectName,
                null,
                owner,
                repo
        );

        List<ReleaseBean> fromGit;
        List<ReleaseBean> fromJira;

        // Both "getAll" methods return all the releases they know
        // We filter only the official ones, present in list from Jira AND in list from Git
        try {
            fromJira = JiraInteraction.getAllReleases(param);
            fromGit = GitInteraction.getAllReleases(param);
        } catch (JiraException | GitException e) {
            throw new ControllerException("Can't retrieve the releases: " + e.getMessage());
        }


        filterReleases(fromJira, fromGit);

        // Printing the user only the considered releases
        userBoundary.printReleases(allReleases);

        // We have printed all the releases, filtered by GitHub and Jira
        userBoundary.printMessage(new MessageBean("Start working on the first 33% of the releases..."));

        int size = allReleases.size();
        int firstThird = size / 3;

        // Taking the first third...
        for (int i = 0; i < firstThird; i++) {
            firstReleases.add(allReleases.get(i));
        }
        mess = new MessageBean("Considered releases will be written into this file: " + firstReleaseFile);
        userBoundary.printMessage(mess);

        userBoundary.printMessage(new MessageBean("The following are the first 33%:"));
        userBoundary.printReleases(firstReleases);

        datasetBuilderController = new DatasetBuilderController(firstReleases);
        datasetBuilderController.setGraphicInterface(userBoundary);
        buildDataset();
    }

    @Override
    public void finish() throws ControllerException {
        // We write on a file the used releases to allow more repetitions of this experiment
        try {
            ReleaseDAO.writeAllReleases(allReleases);
            ReleaseDAO.writeFirstReleases(firstReleases);

        } catch (PersistenceException e) {
            throw new ControllerException(e.getMessage());
        }
    }


    private void setAllReleases(List<ReleaseBean> list) {
        this.allReleases = list;
    }

    private void filterReleases(List<ReleaseBean> jiraList, List<ReleaseBean> gitList) {
        List<ReleaseBean> toSet = new ArrayList<>();
        for (ReleaseBean jira : jiraList) {
            for (ReleaseBean git : gitList) {
                String jiraVersion = jira.getVersion();
                String gitVersion = git.getVersion();
                if (jiraVersion.equalsIgnoreCase(gitVersion)) {
                    toSet.add(jira);
                }
            }
        }

        toSet.sort(Comparator.comparing(ReleaseBean::getReleaseDate)
                .thenComparing(ReleaseBean::getVersion));

        List<ReleaseBean> noMilestones = new ArrayList<>();
        for (ReleaseBean rel : toSet) {
            if (!(rel.getVersion().contains("-M") || rel.getVersion().contains(".M"))) {
                noMilestones.add(rel);
            }
        }

        List<ReleaseBean> filtered = new ArrayList<>();
        LocalDate last = null;

        for (ReleaseBean rel : noMilestones){
            LocalDate newDate = rel.getReleaseDate();

            if(last == null || !newDate.equals(last)){
                filtered.add(rel);
                last = newDate;
            }
        }

        setAllReleases(filtered);
    }

    private void buildDataset() throws ControllerException {
        // The logic to build the dataset
        datasetBuilderController.start();
        datasetBuilderController.finish();
    }

}

