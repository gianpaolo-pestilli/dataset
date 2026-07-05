package boundary.ml;

import bean.ClassesBean;
import bean.ReleaseBean;
import boundary.UserInterface;
import control.AppController;
import control.MLController;

public class MLUserInterface extends UserInterface
{

    @Override
    protected void printClass(Integer i, ClassesBean classBean) {
        //
    }

    @Override
    protected void printRelease(ReleaseBean release, int i) {
        //
    }

    @Override
    protected AppController getController() {
        AppController controller = new MLController();
        controller.setGraphicInterface(this);
        return controller;
    }
}
