package dao;
import bean.ClassesBean;
import exception.ConfigException;
import entity.Class;
import exception.PersistenceException;
import settings.PropertiesSetter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.ZoneId;

public class SmellClassesDAO extends ClassesDAO {

    @Override
    public void saveRanking(List<ClassesBean> classList) throws PersistenceException {
        String filename = getOutputFile();

        try (FileWriter writer = new FileWriter(filename)) {
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            writer.write("Data: " + today.format(formatter) + "\n");
            writer.write("Posizione,Progetto,Classe,Versione,Numero Smell\n");

            for (int i = 0; i < classList.size(); i++) {
                ClassesBean classBean = classList.get(i);
                writer.write((i + 1) + "," +
                        classBean.getProjectName() + "," +
                        classBean.getClassName() + "," +
                        classBean.getReleaseVersion() + "," +
                        classBean.getNumSmell() + "\n");
            }
            PropertiesSetter.writtenOnSmells();

        } catch (IOException | ConfigException e) {
            throw new PersistenceException("Error while writing file: " + filename + e.getMessage());
        }
    }

    @Override
    public String getOutputFile() throws PersistenceException {
        try {
            return PropertiesSetter.getSmellRankingFile();
        } catch (ConfigException e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public List<Class> getAllClasses() throws PersistenceException {
        String filename = getOutputFile().replaceAll("_\\d+\\.csv$", "_0.csv");
        List<Class> classes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    processLine(line, classes);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Can't read smells file: " + e.getMessage());
        }
        return classes;
    }

    private void processLine(String line, List<Class> classes) {
        String[] parts = line.split(",");
        if (parts.length >= 5) {
            String className = parts[2].trim();
            try {
                int numSmell = Integer.parseInt(parts[4].trim());
                classes.add(new Class(className, numSmell));
            } catch (NumberFormatException _) {
                // Ignore
            }
        }
    }
}