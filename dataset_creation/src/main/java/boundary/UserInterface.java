package boundary;

import bean.ClassesBean;
import bean.MessageBean;
import bean.ReleaseBean;
import control.AppController;
import exception.ConfigException;
import exception.ControllerException;
import settings.PropertiesSetter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserInterface {

    // Some colors to highlight the important steps
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    //Remember to always print ANSI_COLOR + text + ANSI_RESET <-- Don't forget this

    public void begin(){
        AppController controller;
        try {
            printInit();
            controller = PropertiesSetter.getController();
            controller.setGraphicInterface(this);
            printControllerSuccess();
            controller.start();
            controller.finish();
            printEnd();
        } catch (ConfigException | ControllerException e) {
            printErrors(e);
        }
    }

    private void printErrors(Exception e){
        System.out.println(ANSI_RED + "????? Sorry, problems occurred ?????"+ ANSI_RESET);
        System.err.println(e.getMessage());;
    }

    private void printInit(){
        System.out.println(ANSI_BLUE + "*********************************" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "Application started ..." + ANSI_RESET);
        System.out.println(ANSI_BLUE + "Controller initialization ..." + ANSI_RESET);
    }

    private void printControllerSuccess(){
        System.out.println(ANSI_BLUE + "Controller is ready ..." + ANSI_RESET);
        System.out.println(ANSI_BLUE + "Work in progress ..." + ANSI_RESET);
    }
    private void printEnd(){
        System.out.println(ANSI_GREEN + "------ Everything is done ------" + ANSI_RESET);
    }

    public void printMessage(MessageBean message){
        System.out.println(message.message());
    }

    public void printClasses(List<ClassesBean> classes){
        Integer number = 1;
        for(ClassesBean classBean : classes){
            printClass(number, classBean);
            number++;
        }
    }

    private void printClass(Integer number, ClassesBean classBean){
        StringBuilder sb = new StringBuilder();

        String n = number.toString();
        n = ANSI_YELLOW + n + ": " + ANSI_RESET;

        String project = classBean.getProjectName();
        project = ANSI_PURPLE + "Progetto = " + project + "--> " + ANSI_RESET;

        String className = classBean.getClassName();
        className = "Classe = " + className + "; ";

        String version = classBean.getReleaseVersion();
        version = "Versione = " + version + "; ";

        String numSmell = classBean.getNumSmell().toString();
        numSmell = ANSI_GREEN + "Numero di smells = " + numSmell + "; " + ANSI_RESET;

        sb.append(n+project+className+version+numSmell);

        Integer time = classBean.getTimeSmell();
        if(time != null){
            // This is from an EffortRankingController, so we have the "time smell"
            String Time = convertTime(time);
            Time = ANSI_RED + "Tempo totale di refactoring = " + Time + "; " + ANSI_RESET;
            sb.append(Time);
        }

        Double debt = classBean.getDebt();
        if(debt != null){
            String Debt = debt.toString();
            Debt = ANSI_CYAN + "Debito tecnico = " + Debt + ANSI_RESET;
            sb.append(Debt);
        }

        String toPrint = sb.toString();
        System.out.println("----------------------------------------------------------");
        System.out.println(toPrint);
        System.out.println("----------------------------------------------------------");
    }

    private String convertTime(Integer time){

        if (time < 60) {
            // Meno di 60 minuti
            return time == 1 ? "1 minuto" : time + " minuti";
        }

        if (time < 1440) {
            // Meno di un giorno: ore + minuti
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
            // Meno di un mese (43200 min = 30 giorni): giorni + ore + minuti
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

        // Più di un mese: mesi + giorni + ore + minuti
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

    public void printReleases(List<ReleaseBean> list){
        int i = 0;
        for(ReleaseBean release : list){
            printRelease(release, i);
            i++;
        }
    }

    private void printRelease(ReleaseBean rel, int i){

        StringBuilder sb = new StringBuilder();
        String n = String.valueOf(i);

        n = ANSI_YELLOW + n + ": " + ANSI_RESET;

        String project = rel.getProjectName();
        project = ANSI_PURPLE + "Progetto = " + project + "--> " + ANSI_RESET;

        String releaseID = rel.getID();
        releaseID = "Release = " + releaseID + "; ";

        String version = rel.getVersion();
        version = "Versione = " + version + "; ";

        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate releaseDate = rel.getReleaseDate();
        String date = releaseDate.format(format);
        date = ANSI_GREEN + "Rilasciata in data = " + date + "; " + ANSI_RESET;
        sb.append(n+project+releaseID+version+date);

        String toPrint = sb.toString();
        System.out.println("----------------------------------------------------------");
        System.out.println(toPrint);
        System.out.println("----------------------------------------------------------");
    }
}
