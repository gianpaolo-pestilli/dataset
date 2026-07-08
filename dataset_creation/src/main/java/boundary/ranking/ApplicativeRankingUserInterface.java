package boundary.ranking;

import bean.ClassesBean;
import bean.ReleaseBean;
import boundary.UserInterface;
import control.AppController;
import control.ApplicativeRankingController;

public class ApplicativeRankingUserInterface extends UserInterface {

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
        AppController controller = new ApplicativeRankingController();
        controller.setGraphicInterface(this);
        return controller;
    }
}
