package dao;

import bean.ClassesBean;
import exception.ConfigException;
import exception.PersistenceException;
import settings.PropertiesSetter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EffortClassesDAO extends ClassesDAO{
    @Override
    public void saveRanking(List<ClassesBean> classList) throws PersistenceException {

        String filename;
        try{
            filename = PropertiesSetter.getEffortRankingFile();
        } catch (ConfigException e) {
            throw new PersistenceException(e.getMessage());
        }

        try (FileWriter writer = new FileWriter(filename)) {
            // Current date
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            writer.write("Data: " + today.format(formatter) + "\n");
            writer.write("Posizione,Progetto,Classe,Versione,Numero Smell,Tempo di Refactoring\n");

            // Classes ordered by smell
            List<ClassesBean> classes = classList;
            for (int i = 0; i < classes.size(); i++) {
                ClassesBean classBean = classes.get(i);
                writer.write((i + 1) + "," +
                        classBean.getProjectName() + "," +
                        classBean.getClassName() + "," +
                        classBean.getReleaseVersion() + "," +
                        classBean.getNumSmell() + "," +
                        classBean.getTimeSmell() + "\n");
            }
            PropertiesSetter.writtenOnEffort();

        } catch (IOException | ConfigException e) {
            throw new PersistenceException("Error while writing file: " + filename + e.getMessage());
        }
    }

    @Override
    public String getOutputFile() throws PersistenceException {
        try {
            return PropertiesSetter.getEffortRankingFile();
        } catch (ConfigException e) {
            throw new PersistenceException(e.getMessage());
        }
    }
}
