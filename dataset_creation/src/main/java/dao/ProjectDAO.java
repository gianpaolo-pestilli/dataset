package dao;

import exception.PersistenceException;
import view.UserInterface;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProjectDAO {

    private static String projectName = null;

    public static String getProject() throws PersistenceException {

        if(projectName == null){
            try (InputStream in = new FileInputStream("config.properties")) {
                Properties prop = new Properties();
                prop.load(in);
                String project = prop.getProperty("project.name").toUpperCase();
                projectName = project;

            } catch (IOException e) {
                throw new PersistenceException("*** Config file is missing ***");
            }
        }
        return projectName;
    }
}
