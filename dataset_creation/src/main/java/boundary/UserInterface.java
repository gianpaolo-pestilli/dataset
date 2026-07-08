package boundary;

import bean.ClassesBean;
import bean.MessageBean;
import bean.ReleaseBean;
import control.AppController;
import exception.ControllerException;
import java.util.List;

public abstract class UserInterface {

    // Some colors to highlight the important steps
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";

    //Remember to always print ANSI_COLOR + text + ANSI_RESET <-- Don't forget this

    public void begin(){
        AppController controller;
        try {
            printInit();
            controller = getController();
            printControllerSuccess();
            controller.start();
            controller.finish();
            printEnd();
        } catch (ControllerException e) {
            printErrors(e);
        }
    }

    private void printErrors(Exception e){
        System.out.println(ANSI_RED + "????? Sorry, problems occurred ?????"+ ANSI_RESET);
        System.err.println(e.getMessage());
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

    public void printReleases(List<ReleaseBean> list){
        int i = 1;
        for(ReleaseBean release : list){
            printRelease(release, i);
            i++;
        }
    }
    protected abstract void printClass(Integer i, ClassesBean classBean);
    protected abstract void printRelease(ReleaseBean release, int i);
    protected abstract AppController getController();

}
