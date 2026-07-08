package boundary.ranking;

import bean.ClassesBean;
import control.AppController;
import control.SmellRankingController;

public class SmellUserInterface extends RankingUserInterface {

    @Override
    public AppController getRankingController() {
        AppController controller = new SmellRankingController();
        controller.setGraphicInterface(this);
        return controller;
    }

    @Override
    public String getTime(ClassesBean classesBean){
        return "";
    }

    @Override
    public String getDebt(ClassesBean classesBean){
        return "";
    }

}
