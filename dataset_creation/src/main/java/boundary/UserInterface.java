package boundary;

import control.AppController;
import control.DatasetCreatorController;
import exception.ConfigException;
import exception.ControllerException;
import settings.PropertiesSetter;

public class UserInterface {

    public void begin(){
        AppController controller;
        try {
            controller = PropertiesSetter.getController();
            controller.start();
            controller.finish();
        } catch (ConfigException |ControllerException e) {
            printErrors(e);
        }
    }

    private void printErrors(Exception e){
        System.out.println("????? Sorry, problems occurred ?????");
        System.err.println(e.getMessage());;
    }

}
