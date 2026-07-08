package boundary.dataset;

import bean.ClassesBean;
import bean.ReleaseBean;
import boundary.UserInterface;
import control.AppController;
import control.DatasetController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatasetUserInterface extends UserInterface {

    @Override
    public AppController getController(){
        AppController toReturn = new DatasetController();
        toReturn.setGraphicInterface(this);
        return toReturn;
    }


    @Override
    public void printClasses(List<ClassesBean> classes) {
        // Nothing to do
    }

    @Override
    protected void printClass(Integer i, ClassesBean classBean) {
        // Nothing to do
    }

    @Override
    protected void printRelease(ReleaseBean rel, int i){

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
