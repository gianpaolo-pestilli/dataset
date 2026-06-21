package dao;

import bean.ClassesBean;
import exception.ConfigException;
import exception.PersistenceException;
import settings.PropertiesSetter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ApplicativeRankingDAO extends ClassesDAO {

    @Override
    public void saveRanking(List<ClassesBean> classList) throws PersistenceException {

        String filename = getOutputFile();

        try (FileWriter writer = new FileWriter(filename)) {
            // Classes are ordered by numSmells
            List<ClassesBean> classes = classList;
            for (int i = 0; i < classes.size(); i++) {
                ClassesBean classBean = classes.get(i);
                writer.write((i + 1) + ":  " +
                        classBean.getClassName() + "  ->  " +
                        classBean.getNumSmell() +"\n");
            }

        } catch (IOException e) {
            throw new PersistenceException("Error while writing file: " + filename + e.getMessage());
        }
    }

    @Override
    public String getOutputFile() throws PersistenceException {
        try {
            return PropertiesSetter.getApplicativeFile();
        } catch (ConfigException e) {
            throw new PersistenceException(e.getMessage());
        }
    }
}
