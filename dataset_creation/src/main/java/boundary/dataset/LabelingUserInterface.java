package boundary.dataset;

import bean.ClassesBean;
import bean.ReleaseBean;
import boundary.UserInterface;
import control.AppController;
import control.LabelingController;

public class LabelingUserInterface extends UserInterface {

    @Override
    protected AppController getController() {
        AppController toReturn = new LabelingController();
        toReturn.setGraphicInterface(this);
        return toReturn;
    }


    @Override
    protected void printClass(Integer i, ClassesBean classBean) {
        //
    }

    @Override
    protected void printRelease(ReleaseBean release, int i) {
        //
    }
}
