package control;

import bean.MessageBean;
import bean.ProjectInfoBean;
import bean.ReleaseBean;
import boundary.GitInteraction;
import boundary.JiraInteraction;
import dao.DatasetDAO;
import dao.ReleaseDAO;
import exception.*;
import settings.PropertiesSetter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*

The ranking of the classes has been done relying on SonarCloud and current release,
for the dataset we want something more accurate, so the first step is about filtering the releases
spotting the discrepancies between Jira and Git and eliminating branches different from the main one

*/

public class DatasetController extends AppController {

    // The following list contains the beans of the FILTERED releases
    private List<ReleaseBean> releases = new ArrayList<>();

    @Override
    public void start() throws ControllerException {
        String filename;
        String projectName;
        String projectKey;
        try {
            filename = DatasetDAO.getFilename();
            projectName = PropertiesSetter.getProjectName();
            projectKey = PropertiesSetter.getSonarKey();

        } catch (ConfigException e) {
            throw new ControllerException("Error while retrieving the file: " + e.getMessage());
        }


        MessageBean mess;

        mess = new MessageBean("Analyzing the project: " + projectName + "...");
        userBoundary.printMessage(mess);

        mess = new MessageBean("\nFiltering releases...");
        userBoundary.printMessage(mess);

        mess = new MessageBean("Everything you will see will also be written into a file");
        userBoundary.printMessage(mess);

        ProjectInfoBean param = new ProjectInfoBean(
                projectKey,
                projectName,
                null
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
        userBoundary.printReleases(releases);
    }

    @Override
    public void finish() throws ControllerException {

        // We write on a file the used releases to allow more repetitions of this experiment
        try {
            ReleaseDAO.writeReleases(releases);
        } catch (PersistenceException e) {
            throw new ControllerException(e.getMessage());
        }
    }


    private void setReleases(List<ReleaseBean> list){
        this.releases = list;
    }

    private void filterReleases(List<ReleaseBean> Jira, List<ReleaseBean> Git){
        List<ReleaseBean> toSet = new ArrayList<>();

        for(ReleaseBean jira : Jira){
            for (ReleaseBean git : Git){
                String jiraVersion = jira.getVersion();
                String gitVersion = git.getVersion();
                if(jiraVersion.equalsIgnoreCase(gitVersion)){
                    toSet.add(jira);
                }
            }
        }

        setReleases(toSet);
    }
}
