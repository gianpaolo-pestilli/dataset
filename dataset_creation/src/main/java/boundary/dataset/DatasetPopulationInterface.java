package boundary.dataset;

import bean.ClassesBean;
import bean.ReleaseBean;
import boundary.UserInterface;
import control.AppController;
import control.DatasetPopulationController;

public class DatasetPopulationInterface extends UserInterface {

    @Override
    protected AppController getController() {
        AppController toReturn = new DatasetPopulationController();
        toReturn.setGraphicInterface(this);
        return toReturn;
    }



    @Override
    protected void printClass(Integer i, ClassesBean classBean) {
        // Nothing to do
    }

    @Override
    protected void printRelease(ReleaseBean release, int i) {
        // Nothing to d
    }
}
