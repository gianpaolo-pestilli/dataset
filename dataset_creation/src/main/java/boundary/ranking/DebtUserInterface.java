package boundary.ranking;

import bean.ClassesBean;
import control.AppController;
import control.DebtRankingController;


public class DebtUserInterface extends EffortUserInterface{

    @Override
    public AppController getRankingController() {
        AppController controller = new DebtRankingController();
        controller.setGraphicInterface(this);
        return controller;
    }

    @Override
    public String getDebt(ClassesBean classBean){
        String debt = classBean.getDebt().toString();
        debt = ANSI_CYAN + "Debito tecnico = " + debt + ANSI_RESET;
        return debt;
    }
}
