package settings;
import control.*;
import exception.ConfigException;

import java.io.*;
import java.util.Properties;

public class PropertiesSetter {

    private static String fileName = "config.properties";

    public static String getProjectName() throws ConfigException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(fileName)) {
            prop.load(input);
            return prop.getProperty("project.name");
        } catch (IOException e) {
            throw new ConfigException("Failed to read project.name from config.properties: " + e.getMessage());
        }
    }

    public static AppController getController() throws ConfigException{
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(fileName)) {
            prop.load(input);
            String controlType = prop.getProperty("controller.type");
            ControllerType controller = ControllerType.valueOf(controlType);

            switch (controller){
                case DATASET -> {return new DatasetCreatorController();}
                case SMELL_RANKING -> {return new SmellRankingController();}
                case EFFORT_RANKING -> {return new EffortRankingController();}
                case DEBT_RANKING -> {return new DebtRankingController();}
                default -> throw new ConfigException("Invalid controller parameters");
            }

        } catch (IOException e) {
            throw new ConfigException("Failed to read controller.type from config.properties: " + e.getMessage());
        }
    }

    // To avoid overriding existent files
    public static String getSmellRankingFile() throws ConfigException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(fileName)) {
            prop.load(input);
            String file = prop.getProperty("smells-ranking.file");
            String number = prop.getProperty("smells-ranking.count");
            String touched = prop.getProperty("smells-ranking.touched");
            Integer written = Integer.parseInt(touched);
            String to_update;
            Integer newOne = Integer.parseInt(number);
            if(written == 1){
                // The latest file has been written, so we have to create a new one
                newOne++;
                prop.setProperty("smells-ranking.touched", "0"); // New file is clear
            }
            to_update = String.valueOf(newOne);
            prop.setProperty("smells-ranking.count", to_update);

            try(OutputStream output = new FileOutputStream(fileName)){
                prop.store(output,null);
            }

            return file + number + ".csv";

        } catch (IOException e) {
            throw new ConfigException("Failed to read smells-ranking.file from config.properties: " + e.getMessage());
        }
    }

    public static String getEffortRankingFile() throws ConfigException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(fileName)) {
            prop.load(input);
            String file = prop.getProperty("effort-ranking.file");
            String number = prop.getProperty("effort-ranking.count");
            String touched = prop.getProperty("effort-ranking.touched");
            Integer written = Integer.parseInt(touched);
            String to_update;
            Integer newOne = Integer.parseInt(number);
            if(written == 1){
                newOne++;
                prop.setProperty("effort-ranking.touched", "0");
            }

            to_update = String.valueOf(newOne);
            prop.setProperty("effort-ranking.count", to_update);

            try(OutputStream output = new FileOutputStream(fileName)){
                prop.store(output,null);
            }

            return file + number + ".csv";

        } catch (IOException e) {
            throw new ConfigException("Failed to read effort-ranking.file from config.properties: " + e.getMessage());
        }
    }

    public static String getSonarKey() throws ConfigException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(fileName)) {
            prop.load(input);
            String projectKey = prop.getProperty("sonar.project.key");
            return projectKey;
        } catch (IOException e) {
            throw new ConfigException("Failed to read sonar.project.key from config.properties: " + e.getMessage());
        }
    }

    public static void writtenOnEffort() throws ConfigException{
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(fileName)) {
            prop.load(input);
            String touched = "1";
            prop.setProperty("effort-ranking.touched", touched);
            try(OutputStream output = new FileOutputStream(fileName)){
                prop.store(output,null);
            }

        } catch (IOException e) {
            throw new ConfigException("Failed to read effort-ranking.file from config.properties: " + e.getMessage());
        }
    }

    public static void writtenOnSmells() throws ConfigException{
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(fileName)) {
            prop.load(input);
            String touched = "1";
            prop.setProperty("smells-ranking.touched", touched);
            try(OutputStream output = new FileOutputStream(fileName)){
                prop.store(output,null);
            }

        } catch (IOException e) {
            throw new ConfigException("Failed to read smells-ranking.file from config.properties: " + e.getMessage());
        }
    }

    public static String getDebtRankingFile() throws ConfigException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(fileName)) {
            prop.load(input);
            String file = prop.getProperty("debt-ranking.file");
            String number = prop.getProperty("debt-ranking.count");
            String touched = prop.getProperty("debt-ranking.touched");
            Integer written = Integer.parseInt(touched);
            String to_update;
            Integer newOne = Integer.parseInt(number);
            if(written == 1){
                newOne++;
                prop.setProperty("debt-ranking.touched", "0");
            }

            to_update = String.valueOf(newOne);
            prop.setProperty("debt-ranking.count", to_update);

            try(OutputStream output = new FileOutputStream(fileName)){
                prop.store(output,null);
            }

            return file + number + ".csv";

        } catch (IOException e) {
            throw new ConfigException("Failed to read debt-ranking.file from config.properties: " + e.getMessage());
        }
    }

    public static void writtenOnDebt() throws ConfigException{
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(fileName)) {
            prop.load(input);
            String touched = "1";
            prop.setProperty("debt-ranking.touched", touched);
            try(OutputStream output = new FileOutputStream(fileName)){
                prop.store(output,null);
            }

        } catch (IOException e) {
            throw new ConfigException("Failed to read debt-ranking.file from config.properties: " + e.getMessage());
        }
    }

}