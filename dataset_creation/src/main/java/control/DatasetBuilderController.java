package control;

import bean.ReleaseBean;
import boundary.UserInterface;
import entity.Release;
import exception.ControllerException;

import java.util.ArrayList;
import java.util.List;

public class DatasetBuilderController extends AppController{

    List<Release> allReleases = new ArrayList<>();
    List<Release> firstReleases = new ArrayList<>();

    public DatasetBuilderController(List<ReleaseBean> all, List<ReleaseBean> first){
        userBoundary.printMessage(new bean.MessageBean("DatasetBuilderController is ready ..."));

        for (ReleaseBean rel : all){
            allReleases.add(new Release(rel.getProjectName(), rel.getReleaseDate(), rel.getID(), rel.getVersion()));
        }
        for (ReleaseBean rel : first){
            firstReleases.add(new Release(rel.getProjectName(), rel.getReleaseDate(), rel.getID(), rel.getVersion()));
        }
    }

    @Override
    public void start() throws ControllerException {
    }

    @Override
    public void finish() throws ControllerException {
    }
}
