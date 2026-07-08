package boundary.ml;


import bean.ClassesBean;
import bean.ReleaseBean;
import boundary.UserInterface;
import control.AppController;
import control.MLDataController;

// Boundary to create datasets for Milestone 3
public class MLDataInterface extends UserInterface{
    @Override
    protected void printClass(Integer i, ClassesBean classBean) {
        // Nothing to do
    }

    @Override
    protected void printRelease(ReleaseBean release, int i) {
        // Nothing to do
    }

    @Override
    protected AppController getController() {
        AppController controller = new MLDataController();
        controller.setGraphicInterface(this);
        return controller;
    }
}
