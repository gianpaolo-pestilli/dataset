package dao;

import dao.dto.ClassDTO;
import entity.Release;
import entity.Class;
import exception.PersistenceException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DatasetDAO {
    private static final String datasetFile = "dataset.csv";
    private static final String sonarDataset = "dataset-sonar.csv";

    public static String getDatasetFilename(){return datasetFile;}

    public static String getSonarDataset(){return sonarDataset;}

    // To create the dataset with the SonarCloud information: the file is empty before this call
    public static void writeReleaseFirstTime(Release release) throws PersistenceException {
        boolean isNewFile = !new File(sonarDataset).exists();

        try (FileWriter fw = new FileWriter(sonarDataset, true);
             PrintWriter pw = new PrintWriter(fw)){

            if (isNewFile) {
                pw.println("ProjectName,ClassName,ReleaseID,numOps,numSmells");
            }

            List<Class> classes = release.getClasses();
            String projectName = release.getProjectName();
            int progressiveNumber = release.getProgressiveNumber();
            for (Class c : classes) {

                StringBuilder sb = new StringBuilder();

                sb.append(projectName).append(",");
                sb.append(c.getName()).append(",");
                sb.append(progressiveNumber).append(",");
                // We will complete the dataset with other iterations
                sb.append(c.getNumOps()).append(",");
                sb.append(c.getNumSmells()).append("");
                pw.println(sb.toString());
            }

        } catch (IOException e) {
            throw new PersistenceException("Error while writing release to sonar-dataset file: " + e.getMessage());
        }
    }

    public static void writeReleaseComplete(Release release) throws PersistenceException {
        boolean isNewFile = !new File(datasetFile).exists();
        try (FileWriter fw = new FileWriter(datasetFile, true);
             PrintWriter pw = new PrintWriter(fw)){

            if (isNewFile) {
                pw.println("ProjectName,ClassName,ReleaseID,"+
                        "LOC,LOCFromBegin,numRevisions,numRevisionsFromBegin,"+
                        "numFixes,numFixesFromBegin,numAuthors,numAuthorsFromBegin,"+
                        "churn,churnFromBegin,maxLOCAdded,maxLOCAddedFromBegin," +
                        "avgLOCAdded,avgLOCAddedFromBegin,avgChangeSet,avgChangeSetFromBegin," +
                        "age,weightedAge,numOps,avgTimeBetweenCommits,numSmells," +
                        "isBuggy");
            }

            List<Class> classes = release.getClasses();
            String projectName = release.getProjectName();
            int progressiveNumber = release.getProgressiveNumber();
            for (Class c : classes) {

                StringBuilder sb = new StringBuilder();

                sb.append(projectName).append(",");
                sb.append(c.getName()).append(",");
                sb.append(progressiveNumber).append(",");
                /*


                OTHER FEATURES


                */
                sb.append(c.getNumOps()).append(",");
                sb.append(c.getNumSmells()).append("");
                pw.println(sb.toString());
            }

        } catch (IOException e) {
            throw new PersistenceException("Error while writing release to dataset file: " + e.getMessage());
        }
    }

    public static List<ClassDTO> getSonarClasses() throws PersistenceException {
        List<ClassDTO> toReturn = new ArrayList<>();
        String filename = sonarDataset;
        String line;
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        br.readLine();
        while ((line = br.readLine()) != null) {
            String[] data = line.split(csvSplitBy);
            if (data.length >= 5) {
                ClassDTO extracted = extractClass(data);
                toReturn.add(extracted);
            }
        }
    } catch (IOException e) {
        throw new PersistenceException("Error occurred while reading file: " + filename +"\n"+e.getMessage());
    }
        return toReturn;
}

private static ClassDTO extractClass(String[] data) {
    String pathName = data[1].trim();
    int progressiveNumber = Integer.parseInt(data[2].trim());
    int numOps = Integer.parseInt(data[3].trim());
    int numSmell = Integer.parseInt(data[4].trim());
    return new ClassDTO(pathName, progressiveNumber, numSmell, numOps);
    }
}


