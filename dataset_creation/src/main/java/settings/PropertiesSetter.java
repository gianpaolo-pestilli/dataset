package settings;

import control.AppController;
import control.DatasetCreatorController;
import control.EffortRankingController;
import control.SmellRankingController;
import exception.ConfigException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesSetter {
    public static String getProjectName() throws ConfigException {
        Properties prop = new Properties();
        String fileName = "config.properties";
        try (InputStream input = new FileInputStream(fileName)) {
            prop.load(input);
            return prop.getProperty("project.name");
        } catch (IOException e) {
            throw new ConfigException("Failed to read project.name from config.properties: " + e.getMessage());
        }
    }

    public static AppController getController() throws ConfigException{
        Properties prop = new Properties();
        String fileName = "config.properties";
        try (InputStream input = new FileInputStream(fileName)) {
            prop.load(input);
            String controlType = prop.getProperty("controller.type");
            ControllerType controller = ControllerType.valueOf(controlType);

            switch (controller){
                case DATASET -> {return new DatasetCreatorController();}
                case SMELL_RANKING -> {return new SmellRankingController();}
                case EFFORT_RANKING -> {return new EffortRankingController();}
                default -> throw new ConfigException("Invalid controller parameters");
            }

        } catch (IOException e) {
            throw new ConfigException("Failed to read controller.type from config.properties: " + e.getMessage());
        }
    }
}