package control;

import boundary.SonarCloudInteraction;
import bean.ClassesBean;
import bean.MessageBean;
import dao.ClassesDAO;
import exception.ConfigException;
import exception.ControllerException;
import exception.PersistenceException;
import exception.SonarException;
import settings.PropertiesSetter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class RankingController extends AppController {

    protected List<ClassesBean> ranking = new ArrayList<>();
    ClassesDAO myDAO = getMyClassDAO();

    @Override
    public void start() throws ControllerException {
        String filename;
        String projectName;
        try{
            filename = myDAO.getOutputFile();
            projectName = PropertiesSetter.getProjectName();

        } catch (ConfigException | PersistenceException e){
            throw new ControllerException("Error while retrieving the output file name: " + e.getMessage());
        }

        MessageBean mess = new MessageBean("Everything you will see will also be written into this file: "
                + filename);
        userBoundary.printMessage(mess);

        mess = new MessageBean("Analyzing the project: " + projectName + "...");
        userBoundary.printMessage(mess);

        List<ClassesBean> list;
        try {
            String currentVersion = SonarCloudInteraction.getCurrentReleaseVersion();
            list = SonarCloudInteraction.getAllClasses(currentVersion); // To complete
        } catch (SonarException e) {
            throw new ControllerException("Error while retrieving classes: " + e.getMessage());
        }

        setRanking(list);

        // In the list there are also files that are not .java  files
        // We want to cut them away

        filterJava();

        // The classes will be sorted according to the son, if it's a SmellRanking every timeSmell field will be null
        sortRanking();

        // Now the ranking is ready, we can print it to the user
        userBoundary.printClasses(ranking);
    }

    @Override
    public void finish() throws ControllerException{

        try{
            myDAO.saveRanking(ranking);
        } catch (PersistenceException e) {
            throw new ControllerException("Error while writing output classes: " + e.getMessage());
        }
    }

    private void filterJava(){
        ranking = ranking.stream()
                .filter(cb -> cb.getClassName() != null && cb.getClassName().endsWith(".java"))
                .collect(Collectors.toList());
    }

    protected void setRanking(List<ClassesBean> classes){
        this.ranking = classes;
    }

    protected abstract void sortRanking();
    protected abstract ClassesDAO getMyClassDAO();

}