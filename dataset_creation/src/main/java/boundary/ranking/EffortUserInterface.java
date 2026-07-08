package boundary.ranking;

import bean.ClassesBean;
import control.AppController;
import control.EffortRankingController;


public class EffortUserInterface extends RankingUserInterface {

    private static final String STR_MINUTI = " minuti";
    private static final String STR_MINUTO = " minuto";
    private static final String STR_ORE = " ore";
    private static final String STR_ORA = " ora";
    private static final String STR_GIORNI = " giorni";
    private static final String STR_GIORNO = " giorno";
    private static final String STR_MESI = " mesi";
    private static final String STR_MESE = " mese";
    private static final String STR_AND = " e ";

    @Override
    public AppController getRankingController() {
        AppController controller = new EffortRankingController();
        controller.setGraphicInterface(this);
        return controller;
    }

    @Override
    public String getTime(ClassesBean classesBean){
        String time = convertTime(classesBean.getTimeSmell());
        return ANSI_RED + "Tempo totale di refactoring = " + time + "; " + ANSI_RESET;
    }

    @Override
    public String getDebt(ClassesBean classBean){
        return "";
    }

    private String convertTime(Integer time){
        if (time == null || time == 0) {
            return "0" + STR_MINUTI;
        }

        int months = time / 43200;
        int remaining = time % 43200;

        int days = remaining / 1440;
        remaining = remaining % 1440;

        int hours = remaining / 60;
        int minutes = remaining % 60;

        StringBuilder result = new StringBuilder();

        appendTimePart(result, months, STR_MESE, STR_MESI);
        appendTimePart(result, days, STR_GIORNO, STR_GIORNI);
        appendTimePart(result, hours, STR_ORA, STR_ORE);
        appendTimePart(result, minutes, STR_MINUTO, STR_MINUTI);

        return result.toString();
    }

    private void appendTimePart(StringBuilder sb, int value, String singular, String plural) {
        if (value > 0) {
            if (!(sb.isEmpty())) {
                sb.append(STR_AND);
            }
            sb.append(value).append(value > 1 ? plural : singular);
        }
    }
}