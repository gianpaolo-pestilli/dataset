package control;
import bean.ClassesBean;
import bean.MessageBean;
import bean.ProjectInfoBean;
import bean.ReleaseBean;
import boundary.api.GitInteraction;
import boundary.api.SonarCloudInteraction;
import dao.DatasetDAO;
import entity.Class;
import entity.Release;
import exception.*;
import settings.PropertiesSetter;

import java.util.ArrayList;
import java.util.List;

public class DatasetBuilderController extends AppController{

    List<Release> firstReleases = new ArrayList<>();

    public DatasetBuilderController(List<ReleaseBean> first){

        for (int i = 0; i < first.size(); i++) {
            ReleaseBean rel = first.get(i);
            Release newOne = new Release(rel.getProjectName(), rel.getReleaseDate(), rel.getID(), rel.getVersion());
            newOne.setProgressiveNumber(i + 1);
            firstReleases.add(newOne);
        }
    }

    @Override
    public void start() throws ControllerException {
        userBoundary.printMessage(new bean.MessageBean("DatasetBuilderController is ready ..."));
        String projectName;
        String projectKey;
        String projectOwner;
        String path;
        String token;
        String sonar;
        try {
            projectName = PropertiesSetter.getProjectName();
            projectKey = PropertiesSetter.getSonarKey();
            projectOwner = PropertiesSetter.getOwner();
            path = PropertiesSetter.getProjectLocalPath();
            token = PropertiesSetter.getSonarToken();
            sonar = PropertiesSetter.getSonarPath();

        } catch (ConfigException e) {
            throw new ControllerException(e.getMessage());
        }

        ProjectInfoBean info = new ProjectInfoBean(projectKey, projectName, null, projectOwner, null);
        info.setToken(token);
        info.setLocalPath(path);
        info.setSonar(sonar);
        try {

            for (Release rel : firstReleases) {
                userBoundary.printMessage(new MessageBean("Analyzing release number: " + rel.getProgressiveNumber()));
                info.setReleaseVersion(rel.getVersion());
                checkout(info);
                // After checkout we have to check for SonarCloud output
                List<ClassesBean> classes = SonarCloudInteraction.getClassesFromCurrentRelease(info);
                for(ClassesBean bean : classes){
                    rel.addClass(bean.getClassName());
                }
                List<Class> realClasses = rel.getClasses();
                for(int i = 0; i < classes.size(); i++){
                    Class interest = realClasses.get(i);
                    interest.setNumSmells(classes.get(i).getNumSmell());
                    interest.setNumOps(classes.get(i).getNumOps());
                }
            writeRelease(rel);

            }
        } catch (SonarException e) {
            throw new ControllerException(e.getMessage());
        }
    }

    @Override
    public void finish() throws ControllerException {
        String filename = DatasetDAO.getSonarDataset();
        MessageBean mess = new MessageBean("First steps of dataset building are in this file: " +filename);
    }

    private void checkout(ProjectInfoBean project) throws ControllerException {
        try{

            GitInteraction.doCheckout(project);
            SonarCloudInteraction.runScan(project, this);
            SonarCloudInteraction.waitForAnalysisCompletion(project, this);

        } catch (GitException | SonarException e) {
            throw new ControllerException(e.getMessage());
        }


    }

    private void writeRelease(Release release)throws ControllerException{
        try{
            DatasetDAO.writeReleaseFirstTime(release);
        } catch (PersistenceException e) {
            throw new ControllerException(e.getMessage());
        }
    }

}
