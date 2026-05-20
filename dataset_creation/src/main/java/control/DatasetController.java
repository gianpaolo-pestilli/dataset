package control;

import bean.MessageBean;
import bean.ReleaseBean;
import dao.DatasetDAO;
import exception.ConfigException;
import exception.ControllerException;
import settings.PropertiesSetter;

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
        try {
            filename = DatasetDAO.getFilename();
            projectName = PropertiesSetter.getProjectName();

        } catch (ConfigException e) {
            throw new ControllerException("Error while retrieving the file: " + e.getMessage());
        }

        MessageBean mess = new MessageBean("Everything you will see will also be written into this file: "
                + filename);
        userBoundary.printMessage(mess);

        mess = new MessageBean("Analyzing the project: " + projectName + "...");
        userBoundary.printMessage(mess);

        mess = new MessageBean("\nFiltering releases...");
        userBoundary.printMessage(mess);

        /* This needs to be developed
        // Need to understand if projectName is needed to call this methods
        List<ReleaseBean> fromGit = GitInteraction.getAllReleases();
        List<ReleaseBean> fromJira = JiraInteraction.getAllReleases();

        // BOTH THE GETALL-RELEASES METHODS DON'T RETURN OTHER BRANCHES DIFFERENT FROM MAIN ONE
        // THEY ONLY RETURN THE OFFICIAL RELEASES COMMITTED, NOT EVERY COMMITTED RELEASE.
        // In other words, versions ending with "incubating" or "SNAPSHOT", ... are absent

        filterReleases(fromJira, fromGit);

        */

        // Printing the user only the considered releases
        userBoundary.printReleases(releases);
    }

    @Override
    public void finish() throws ControllerException {

        // We write on a file the used releases to allow more repetitions of this experiment

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
