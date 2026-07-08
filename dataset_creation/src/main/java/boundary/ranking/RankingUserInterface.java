package boundary.ranking;

import bean.ClassesBean;
import bean.ReleaseBean;
import boundary.UserInterface;
import control.AppController;


public abstract class RankingUserInterface extends UserInterface {

    @Override
    protected AppController getController(){
        return getRankingController();
    }

    @Override
    protected void printClass(Integer number, ClassesBean classBean){
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


        String time = getTime(classBean);
        String debt = getDebt(classBean);
        sb.append(time+debt);


        String toPrint = sb.toString();
        System.out.println("----------------------------------------------------------");
        System.out.println(toPrint);
        System.out.println("----------------------------------------------------------");
    }


    @Override
    protected void printRelease(ReleaseBean release, int i) {}

    public abstract AppController getRankingController();

    public abstract String getTime(ClassesBean classBean);

    public abstract String getDebt(ClassesBean classBean);


}
