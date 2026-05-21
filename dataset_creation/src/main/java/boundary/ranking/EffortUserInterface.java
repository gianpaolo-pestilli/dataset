package boundary.ranking;

import bean.ClassesBean;
import control.AppController;
import control.EffortRankingController;


public class EffortUserInterface extends RankingUserInterface {

    @Override
    public AppController getRankingController() {
        AppController controller = new EffortRankingController();
        controller.setGraphicInterface(this);
        return controller;
    }

    @Override
    public String getTime(ClassesBean classesBean){
            String Time = convertTime(classesBean.getTimeSmell());
            Time = ANSI_RED + "Tempo totale di refactoring = " + Time + "; " + ANSI_RESET;
            return Time;
    }


    @Override
    public String getDebt(ClassesBean classBean){
        return "";
    }

    private String convertTime(Integer time){

        if (time < 60) {
            // Less than 60 minutes
            return time == 1 ? "1 minuto" : time + " minuti";
        }

        if (time < 1440) {
            // Less than a day
            int hours = time / 60;
            int minutes = time % 60;

            StringBuilder result = new StringBuilder();
            result.append(hours).append(hours > 1 ? " ore" : " ora");

            if (minutes > 0) {
                result.append(" e ").append(minutes).append(minutes > 1 ? " minuti" : " minuto");
            }

            return result.toString();
        }

        if (time < 43200) {
            // Less than a month
            int days = time / 1440;
            int remainingMinutes = time % 1440;
            int hours = remainingMinutes / 60;
            int minutes = remainingMinutes % 60;

            StringBuilder result = new StringBuilder();
            result.append(days).append(days > 1 ? " giorni" : " giorno");

            if (hours > 0) {
                result.append(" e ").append(hours).append(hours > 1 ? " ore" : " ora");
            }

            if (minutes > 0) {
                result.append(" e ").append(minutes).append(minutes > 1 ? " minuti" : " minuto");
            }

            return result.toString();
        }

        // More than a month
        int months = time / 43200;
        int remainingMinutes = time % 43200;
        int days = remainingMinutes / 1440;
        remainingMinutes = remainingMinutes % 1440;
        int hours = remainingMinutes / 60;
        int minutes = remainingMinutes % 60;

        StringBuilder result = new StringBuilder();
        result.append(months).append(months > 1 ? " mesi" : " mese");

        if (days > 0) {
            result.append(" e ").append(days).append(days > 1 ? " giorni" : " giorno");
        }

        if (hours > 0) {
            result.append(" e ").append(hours).append(hours > 1 ? " ore" : " ora");
        }

        if (minutes > 0) {
            result.append(" e ").append(minutes).append(minutes > 1 ? " minuti" : " minuto");
        }

        return result.toString();
    }
}
