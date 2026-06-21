package control;

import bean.MessageBean;
import bean.ProjectInfoBean;
import boundary.api.GitInteraction;
import dao.DatasetDAO;
import dao.ReleaseDAO;
import dao.dto.ClassDTO;
import entity.Class;
import entity.Release;
import exception.ConfigException;
import exception.ControllerException;
import exception.GitException;
import exception.PersistenceException;
import settings.PropertiesSetter;

import java.util.ArrayList;
import java.util.List;

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
            info.setLocalPath(path);
            GitInteraction.getGit(info);
            List<Release> releases = ReleaseDAO.getAllReleases();
            List<ClassDTO> classes = DatasetDAO.getSonarClasses();
            List<ClassDTO> filtered = filter(classes);
            assemble(releases,filtered);
            this.releases = releases;
            addFeatures();

        } catch (PersistenceException | ConfigException | GitException e) {
            throw new ControllerException("Error occurred while fetching releases: " + e.getMessage());
        }finally{
            GitInteraction.close();
        }

    }
    @Override
    public void finish() throws ControllerException {
        // Write on file
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
                    rel.addClass(cls.getPath(),cls.getNumSmells(),cls.getNumMethods());
                }
            }
        }
    }

    private void addFeatures(){
    //
    }



}
