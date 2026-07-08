package settings;

import boundary.UserInterface;
import exception.ConfigException;

public class ApplicationStarter {

    private ApplicationStarter() {
        // Private constructor to prevent instantiation
    }

    public static void launch() {
        try{
            UserInterface ui = PropertiesSetter.getUI();
            ui.begin();
        } catch (ConfigException e) {
            System.err.println("FATAL ERROR, THE APPLICATION CAN'T START");
            System.out.println(e.getMessage());
        }
    }
}
