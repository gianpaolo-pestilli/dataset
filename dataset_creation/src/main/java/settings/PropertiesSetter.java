package settings;
import boundary.UserInterface;
import boundary.dataset.DatasetPopulationInterface;
import boundary.dataset.DatasetUserInterface;
import boundary.dataset.LabelingUserInterface;
import boundary.ml.*;
import boundary.ranking.ApplicativeRankingUserInterface;
import boundary.ranking.DebtUserInterface;
import boundary.ranking.EffortUserInterface;
import boundary.ranking.SmellUserInterface;
import exception.ConfigException;

import java.io.*;
import java.util.Properties;

public class PropertiesSetter {

    private static final String FILENAME = "config.properties";
    private static final String SECRET_FILE = "secrets.properties";

    // Costanti per le chiavi del file di configurazione (per eliminare le Magic Strings)
    private static final String KEY_SMELLS_TOUCHED = "smells-ranking.touched";
    private static final String KEY_SMELLS_FILE = "smells-ranking.file";
    private static final String KEY_SMELLS_COUNT = "smells-ranking.count";

    private static final String KEY_EFFORT_TOUCHED = "effort-ranking.touched";
    private static final String KEY_EFFORT_FILE = "effort-ranking.file";
    private static final String KEY_EFFORT_COUNT = "effort-ranking.count";

    private static final String KEY_DEBT_TOUCHED = "debt-ranking.touched";
    private static final String KEY_DEBT_FILE = "debt-ranking.file";
    private static final String KEY_DEBT_COUNT = "debt-ranking.count";


    public static String getProjectName() throws ConfigException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(FILENAME)) {
            prop.load(input);
            return prop.getProperty("project.name");
        } catch (IOException e) {
            throw new ConfigException("Failed to read project.name from config.properties: " + e.getMessage());
        }
    }

    public static UserInterface getUI() throws ConfigException{
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(FILENAME)) {
            prop.load(input);
            String type = prop.getProperty("application.type");
            ApplicationType app = ApplicationType.valueOf(type);

            switch (app){
                case DATASET_INIT -> {return new DatasetUserInterface();}
                case DATASET_POPULATION -> {return new DatasetPopulationInterface();}
                case SMELL_RANKING -> {return new SmellUserInterface();}
                case EFFORT_RANKING -> {return new EffortUserInterface();}
                case DEBT_RANKING -> {return new DebtUserInterface();}
                case APPLICATIVE_RANKING -> {return new ApplicativeRankingUserInterface();}
                case DATASET_LABELING -> {return new LabelingUserInterface();}
                case ML_TRAINING ->{return new MLUserInterface();}
                case ML_REPORTING -> {return new MLReportInterface();}
                case ML_DATA_PARTITION ->{return new MLDataInterface();}
                case ML_WHAT_IF -> {return new MLWhatIfInterface();}
                default -> throw new ConfigException("Invalid application parameters");
            }

        } catch (IOException e) {
            throw new ConfigException("Failed to read application.type from config.properties: " + e.getMessage());
        }
    }

    public static String getSmellRankingFile() throws ConfigException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(FILENAME)) {
            prop.load(input);
            String file = prop.getProperty(KEY_SMELLS_FILE);
            String number = prop.getProperty(KEY_SMELLS_COUNT);
            String touched = prop.getProperty(KEY_SMELLS_TOUCHED);

            Integer written = Integer.parseInt(touched);
            Integer newOne = Integer.parseInt(number);
            if(written == 1){
                newOne++;
                prop.setProperty(KEY_SMELLS_TOUCHED, "0");
            }
            prop.setProperty(KEY_SMELLS_COUNT, String.valueOf(newOne));

            try(OutputStream output = new FileOutputStream(FILENAME)){
                prop.store(output,null);
            }

            return file + number + ".csv";

        } catch (IOException e) {
            throw new ConfigException("Failed to read smells-ranking config from config.properties: " + e.getMessage());
        }
    }

    public static String getEffortRankingFile() throws ConfigException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(FILENAME)) {
            prop.load(input);
            String file = prop.getProperty(KEY_EFFORT_FILE);
            String number = prop.getProperty(KEY_EFFORT_COUNT);
            String touched = prop.getProperty(KEY_EFFORT_TOUCHED);

            Integer written = Integer.parseInt(touched);
            Integer newOne = Integer.parseInt(number);
            if(written == 1){
                newOne++;
                prop.setProperty(KEY_EFFORT_TOUCHED, "0");
            }

            prop.setProperty(KEY_EFFORT_COUNT, String.valueOf(newOne));

            try(OutputStream output = new FileOutputStream(FILENAME)){
                prop.store(output,null);
            }

            return file + number + ".csv";

        } catch (IOException e) {
            throw new ConfigException("Failed to read effort-ranking config from config.properties: " + e.getMessage());
        }
    }

    public static String getSonarKey() throws ConfigException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(FILENAME)) {
            prop.load(input);
            return prop.getProperty("sonar.project.key");
        } catch (IOException e) {
            throw new ConfigException("Failed to read sonar.project.key from config.properties: " + e.getMessage());
        }
    }

    public static void writtenOnEffort() throws ConfigException{
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(FILENAME)) {
            prop.load(input);
            prop.setProperty(KEY_EFFORT_TOUCHED, "1");
            try(OutputStream output = new FileOutputStream(FILENAME)){
                prop.store(output,null);
            }
        } catch (IOException e) {
            throw new ConfigException("Failed to update effort-ranking.touched: " + e.getMessage());
        }
    }

    public static void writtenOnSmells() throws ConfigException{
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(FILENAME)) {
            prop.load(input);
            prop.setProperty(KEY_SMELLS_TOUCHED, "1");
            try(OutputStream output = new FileOutputStream(FILENAME)){
                prop.store(output,null);
            }
        } catch (IOException e) {
            throw new ConfigException("Failed to update smells-ranking.touched: " + e.getMessage());
        }
    }

    public static String getDebtRankingFile() throws ConfigException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(FILENAME)) {
            prop.load(input);
            String file = prop.getProperty(KEY_DEBT_FILE);
            String number = prop.getProperty(KEY_DEBT_COUNT);
            String touched = prop.getProperty(KEY_DEBT_TOUCHED);

            Integer written = Integer.parseInt(touched);
            Integer newOne = Integer.parseInt(number);
            if(written == 1){
                newOne++;
                prop.setProperty(KEY_DEBT_TOUCHED, "0");
            }

            prop.setProperty(KEY_DEBT_COUNT, String.valueOf(newOne));

            try(OutputStream output = new FileOutputStream(FILENAME)){
                prop.store(output,null);
            }

            return file + number + ".csv";

        } catch (IOException e) {
            throw new ConfigException("Failed to read debt-ranking config from config.properties: " + e.getMessage());
        }
    }

    public static void writtenOnDebt() throws ConfigException{
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(FILENAME)) {
            prop.load(input);
            prop.setProperty(KEY_DEBT_TOUCHED, "1");
            try(OutputStream output = new FileOutputStream(FILENAME)){
                prop.store(output,null);
            }
        } catch (IOException e) {
            throw new ConfigException("Failed to update debt-ranking.touched: " + e.getMessage());
        }
    }

    public static String getOwner() throws ConfigException{
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(SECRET_FILE)) {
            prop.load(input);
            return prop.getProperty("project.owner");
        } catch (IOException e) {
            throw new ConfigException("Failed to read project.owner from secret file: " + e.getMessage());
        }
    }

    public static String getRepo() throws ConfigException{
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(SECRET_FILE)) {
            prop.load(input);
            return prop.getProperty("project.repo");
        } catch (IOException e) {
            throw new ConfigException("Failed to read project.repo from secret file: " + e.getMessage());
        }
    }

    public static String getSonarToken() throws ConfigException{
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(SECRET_FILE)) {
            prop.load(input);
            return prop.getProperty("token");
        } catch (IOException e) {
            throw new ConfigException("Failed to read secret file: " + e.getMessage());
        }
    }

    public static String getProjectLocalPath() throws ConfigException{
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(SECRET_FILE)) {
            prop.load(input);
            return prop.getProperty("path");
        } catch (IOException e) {
            throw new ConfigException("Failed to read path from secret file: " + e.getMessage());
        }
    }

    public static String getApplicativeFile() throws ConfigException{
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(FILENAME)) {
            prop.load(input);
            return prop.getProperty("applicative-ranking.file");
        } catch (IOException e) {
            throw new ConfigException("Failed to read applicative-ranking.file: " + e.getMessage());
        }
    }

    public static String getSonarPath() throws ConfigException {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(SECRET_FILE)) {
            prop.load(input);
            return prop.getProperty("sonar.path");
        } catch (IOException e) {
            throw new ConfigException("Failed to read sonar.path from secret file: " + e.getMessage());
        }
    }
}