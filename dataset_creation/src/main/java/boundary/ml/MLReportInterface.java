package boundary.ml;

import boundary.UserInterface;
import control.AppController;
import control.MLReportingController;

public class MLReportInterface extends UserInterface {
    @Override
    protected void printClass(Integer i, bean.ClassesBean classBean) {
        //
    }

    @Override
    protected void printRelease(bean.ReleaseBean release, int i) {
        //
    }

    @Override
    protected control.AppController getController() {
        AppController controller = new MLReportingController();
        controller.setGraphicInterface(this);
        return controller;
    }
}
