package dao;

import dao.dto.ClassDTO;
import entity.Release;
import entity.Class;
import exception.PersistenceException;
import settings.DataType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DatasetDAO {
    private static final String DATASET_FILE = "dataset.csv";
    private static final String SONAR_DATASET = "dataset-sonar.csv";
    private static final String OFFICIAL_DATASET = "dataset-official.csv";

    private static final String DATASET_B_PLUS = "dataset_B_plus.csv";
    private static final String DATASET_C = "dataset_C.csv";
    private static final String DATASET_B = "dataset_B.csv";

    public static String getDatasetFilename(){return DATASET_FILE;}

    public static String getSonarDataset(){return SONAR_DATASET;}

    // To create the dataset with the SonarCloud information: the file is empty before this call
    public static void writeReleaseFirstTime(Release release) throws PersistenceException {
        boolean isNewFile = !new File(SONAR_DATASET).exists();

        try (FileWriter fw = new FileWriter(SONAR_DATASET, true);
             PrintWriter pw = new PrintWriter(fw)){

            if (isNewFile) {
                pw.println("ProjectName,ClassName,ReleaseID,numOps,numSmells,LOC");
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
                sb.append(c.getNumSmells()).append(",");
                sb.append(c.getLOC());
                pw.println(sb.toString());
            }

        } catch (IOException e) {
            throw new PersistenceException("Error while writing release to sonar-dataset file: " + e.getMessage());
        }
    }

    private static void writeReleaseComplete(Release release) throws PersistenceException {
        boolean isNewFile = !new File(DATASET_FILE).exists();
        try (FileWriter fw = new FileWriter(DATASET_FILE, true);
             PrintWriter pw = new PrintWriter(fw)){

            if (isNewFile) {
                pw.println("ProjectName,ClassName,ReleaseID," +
                        "LOC,numRevisions,numRevisionsFromBegin," +
                        "numFixes,numFixesFromBegin,numAuthors,numAuthorsFromBegin," +
                        "churn,churnFromBegin,maxLOCAdded,maxLOCAddedFromBegin," +
                        "avgLOCAdded,avgLOCAddedFromBegin,avgChangeSet,avgChangeSetFromBegin," +
                        "maxChangeSet,maxChangeSetFromBegin,age,weightedAge," +
                        "numOps,avgTimeBetweenCommits,numSmells,isBuggy");
            }

            List<Class> classes = release.getClasses();
            String projectName = release.getProjectName();
            int progressiveNumber = release.getProgressiveNumber();

            for (Class c : classes) {
                StringBuilder sb = new StringBuilder();

                sb.append(projectName).append(",");
                sb.append(c.getName()).append(",");
                sb.append(progressiveNumber).append(",");

                sb.append(c.getLOC()).append(",");
                sb.append(c.getNumRevisions()).append(",");
                sb.append(c.getNumRevisionsFromBegin()).append(",");
                sb.append(c.getNumFixes()).append(",");
                sb.append(c.getNumFixesFromBegin()).append(",");
                sb.append(c.getNumAuthors()).append(",");
                sb.append(c.getNumAuthorsFromBegin()).append(",");
                sb.append(c.getChurn()).append(",");
                sb.append(c.getChurnFromBegin()).append(",");
                sb.append(c.getMaxLOCAdded()).append(",");
                sb.append(c.getMaxLOCAddedFromBegin()).append(",");
                sb.append(c.getAvgLOCAdded()).append(",");
                sb.append(c.getAvgLOCAddedFromBegin()).append(",");
                sb.append(c.getAvgChangeSet()).append(",");
                sb.append(c.getAvgChangeSetFromBegin()).append(",");
                sb.append(c.getMaxChangeSet()).append(",");
                sb.append(c.getMaxChangeSetFromBegin()).append(",");
                sb.append(c.getAge()).append(",");
                sb.append(c.getWeightedAge()).append(",");

                sb.append(c.getNumOps()).append(",");
                sb.append(c.getAvgTimeBetweenCommits()).append(",");
                sb.append(c.getNumSmells()).append(",");

                // Forzato a false come richiesto
                sb.append("false");

                pw.println(sb.toString());
            }

        } catch (IOException e) {
            throw new PersistenceException("Error while writing release to dataset file: " + e.getMessage());
        }
    }

    public static List<ClassDTO> getSonarClasses() throws PersistenceException {
        List<ClassDTO> toReturn = new ArrayList<>();
        String filename = SONAR_DATASET;
        String line;
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine();
            if (header == null) {
                return toReturn; // File vuoto, evitiamo di proseguire
            }

            while ((line = br.readLine()) != null) {
                String[] data = line.split(csvSplitBy);
                if (data.length >= 6) {
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
        int loc = Integer.parseInt(data[5].trim());
        return new ClassDTO(pathName, progressiveNumber, numSmell, numOps, loc);
    }

    public static void writeDataset(List<Release> releases) throws PersistenceException {
        for (Release rel : releases) {
            writeReleaseComplete(rel);
        }
    }

    public static void writeFinalDataset(List<Release> releases) throws PersistenceException {
        for (Release rel : releases) {
            writeLabeledRelease(rel);
        }
    }

    private static void writeLabeledRelease(Release release) throws PersistenceException {

        boolean isNewFile = !new File(OFFICIAL_DATASET).exists();
        try (FileWriter fw = new FileWriter(OFFICIAL_DATASET, true);
             PrintWriter pw = new PrintWriter(fw)){

            if (isNewFile) {
                pw.println("ProjectName,ClassName,ReleaseID," +
                        "LOC,numRevisions,numRevisionsFromBegin," +
                        "numFixes,numFixesFromBegin,numAuthors,numAuthorsFromBegin," +
                        "churn,churnFromBegin,maxLOCAdded,maxLOCAddedFromBegin," +
                        "avgLOCAdded,avgLOCAddedFromBegin,avgChangeSet,avgChangeSetFromBegin," +
                        "maxChangeSet,maxChangeSetFromBegin,age,weightedAge," +
                        "numOps,avgTimeBetweenCommits,numSmells,isBuggy");
            }

            List<Class> classes = release.getClasses();
            String projectName = release.getProjectName();
            int progressiveNumber = release.getProgressiveNumber();

            for (Class c : classes) {
                StringBuilder sb = new StringBuilder();

                sb.append(projectName).append(",");
                sb.append(c.getName()).append(",");
                sb.append(progressiveNumber).append(",");

                sb.append(c.getLOC()).append(",");
                sb.append(c.getNumRevisions()).append(",");
                sb.append(c.getNumRevisionsFromBegin()).append(",");
                sb.append(c.getNumFixes()).append(",");
                sb.append(c.getNumFixesFromBegin()).append(",");
                sb.append(c.getNumAuthors()).append(",");
                sb.append(c.getNumAuthorsFromBegin()).append(",");
                sb.append(c.getChurn()).append(",");
                sb.append(c.getChurnFromBegin()).append(",");
                sb.append(c.getMaxLOCAdded()).append(",");
                sb.append(c.getMaxLOCAddedFromBegin()).append(",");
                sb.append(c.getAvgLOCAdded()).append(",");
                sb.append(c.getAvgLOCAddedFromBegin()).append(",");
                sb.append(c.getAvgChangeSet()).append(",");
                sb.append(c.getAvgChangeSetFromBegin()).append(",");
                sb.append(c.getMaxChangeSet()).append(",");
                sb.append(c.getMaxChangeSetFromBegin()).append(",");
                sb.append(c.getAge()).append(",");
                sb.append(c.getWeightedAge()).append(",");

                sb.append(c.getNumOps()).append(",");
                sb.append(c.getAvgTimeBetweenCommits()).append(",");
                sb.append(c.getNumSmells()).append(",");

                sb.append(c.isBuggy());

                pw.println(sb.toString());
            }

        } catch (IOException e) {
            throw new PersistenceException("Error while writing release to dataset file: " + e.getMessage());
        }
    }

    public static List<Release> getDataset() throws PersistenceException {
        List<Release> releases = new ArrayList<>();
        int currentReleaseId = 0;
        Release currentRelease = null;
        String line;
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(DATASET_FILE))) {
            // Salta l'intestazione iniziale del file CSV assicurandosi che non sia vuoto
            String header = br.readLine();
            if (header == null) {
                return releases;
            }

            while ((line = br.readLine()) != null) {
                String[] data = line.split(csvSplitBy);

                if (data.length >= 26) {
                    int releaseId = Integer.parseInt(data[2].trim());

                    // Se incontriamo un nuovo ReleaseID, istanziamo un nuovo oggetto Release
                    if (currentReleaseId != releaseId) {
                        currentRelease = new Release(data[0].trim(),releaseId);

                        // L'età viene letta dal CSV e impostata alla Release una sola volta
                        currentRelease.setAge(Long.parseLong(data[20].trim()));

                        releases.add(currentRelease);
                        currentReleaseId = releaseId;
                    }

                    String name = data[1].trim();
                    int loc = Integer.parseInt(data[3].trim());
                    int numOps = Integer.parseInt(data[22].trim());
                    int numSmells = Integer.parseInt(data[24].trim());

                    Class c = new Class(currentRelease,name,numSmells,numOps,loc);
                    c.setNumRevisions(Long.parseLong(data[4].trim()));

                    c.setNumRevisionsFromBegin(Long.parseLong(data[5].trim()));

                    c.setNumFixes(Long.parseLong(data[6].trim()));
                    c.setNumFixesFromBegin(Long.parseLong(data[7].trim()));
                    c.setNumAuthors(Long.parseLong(data[8].trim()));
                    c.setNumAuthorsFromBegin(Long.parseLong(data[9].trim()));
                    c.setChurn(Long.parseLong(data[10].trim()));
                    c.setChurnFromBegin(Long.parseLong(data[11].trim()));
                    c.setMaxLOCAdded(Long.parseLong(data[12].trim()));
                    c.setMaxLOCAddedFromBegin(Long.parseLong(data[13].trim()));
                    c.setAvgLOCAdded(Double.parseDouble(data[14].trim()));
                    c.setAvgLOCAddedFromBegin(Double.parseDouble(data[15].trim()));
                    c.setAvgChangeSet(Double.parseDouble(data[16].trim()));
                    c.setAvgChangeSetFromBegin(Double.parseDouble(data[17].trim()));
                    c.setMaxChangeSet(Double.parseDouble(data[18].trim()));
                    c.setMaxChangeSetFromBegin(Double.parseDouble(data[19].trim()));

                    c.setWAge(Double.parseDouble(data[21].trim()));
                    c.setAvgTimeBetweenCommits(Double.parseDouble(data[23].trim()));
                    c.setBuggy(Boolean.parseBoolean(data[25].trim()));

                    currentRelease.addClass(c);
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new PersistenceException("Errore critico durante la ricostruzione del dataset dal file: " + e.getMessage());
        }

        return releases;
    }


    public static List<Release> getOfficialDataset() throws PersistenceException {
        List<Release> releases = new ArrayList<>();
        int currentReleaseId = 0;
        Release currentRelease = null;
        String line;
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(OFFICIAL_DATASET))) {
            // Salta l'intestazione iniziale del file CSV assicurandosi che non sia vuoto
            String header = br.readLine();
            if (header == null) {
                return releases;
            }

            while ((line = br.readLine()) != null) {
                String[] data = line.split(csvSplitBy);

                if (data.length >= 26) {
                    int releaseId = Integer.parseInt(data[2].trim());

                    // Se incontriamo un nuovo ReleaseID, istanziamo un nuovo oggetto Release
                    if (currentReleaseId != releaseId) {
                        currentRelease = new Release(data[0].trim(),releaseId);

                        // L'età viene letta dal CSV e impostata alla Release una sola volta
                        currentRelease.setAge(Long.parseLong(data[20].trim()));

                        releases.add(currentRelease);
                        currentReleaseId = releaseId;
                    }

                    String name = data[1].trim();
                    int loc = Integer.parseInt(data[3].trim());
                    int numOps = Integer.parseInt(data[22].trim());
                    int numSmells = Integer.parseInt(data[24].trim());

                    Class c = new Class(currentRelease,name,numSmells,numOps,loc);
                    c.setNumRevisions(Long.parseLong(data[4].trim()));

                    c.setNumRevisionsFromBegin(Long.parseLong(data[5].trim()));

                    c.setNumFixes(Long.parseLong(data[6].trim()));
                    c.setNumFixesFromBegin(Long.parseLong(data[7].trim()));
                    c.setNumAuthors(Long.parseLong(data[8].trim()));
                    c.setNumAuthorsFromBegin(Long.parseLong(data[9].trim()));
                    c.setChurn(Long.parseLong(data[10].trim()));
                    c.setChurnFromBegin(Long.parseLong(data[11].trim()));
                    c.setMaxLOCAdded(Long.parseLong(data[12].trim()));
                    c.setMaxLOCAddedFromBegin(Long.parseLong(data[13].trim()));
                    c.setAvgLOCAdded(Double.parseDouble(data[14].trim()));
                    c.setAvgLOCAddedFromBegin(Double.parseDouble(data[15].trim()));
                    c.setAvgChangeSet(Double.parseDouble(data[16].trim()));
                    c.setAvgChangeSetFromBegin(Double.parseDouble(data[17].trim()));
                    c.setMaxChangeSet(Double.parseDouble(data[18].trim()));
                    c.setMaxChangeSetFromBegin(Double.parseDouble(data[19].trim()));

                    c.setWAge(Double.parseDouble(data[21].trim()));
                    c.setAvgTimeBetweenCommits(Double.parseDouble(data[23].trim()));
                    c.setBuggy(Boolean.parseBoolean(data[25].trim()));

                    currentRelease.addClass(c);
                }
            }
        } catch (IOException | NumberFormatException e) {
            throw new PersistenceException("Errore critico durante la ricostruzione del dataset dal file: " + e.getMessage());
        }

        return releases;
    }

    private static void writeUpdateRelease(Release release,String filename) throws PersistenceException {

        boolean isNewFile = !new File(filename).exists();
        try (FileWriter fw = new FileWriter(filename, true);
             PrintWriter pw = new PrintWriter(fw)){

            if (isNewFile) {
                pw.println("ProjectName,ClassName,ReleaseID," +
                        "LOC,numRevisions,numRevisionsFromBegin," +
                        "numFixes,numFixesFromBegin,numAuthors,numAuthorsFromBegin," +
                        "churn,churnFromBegin,maxLOCAdded,maxLOCAddedFromBegin," +
                        "avgLOCAdded,avgLOCAddedFromBegin,avgChangeSet,avgChangeSetFromBegin," +
                        "maxChangeSet,maxChangeSetFromBegin,age,weightedAge," +
                        "numOps,avgTimeBetweenCommits,numSmells,isBuggy");
            }

            List<Class> classes = release.getClasses();
            String projectName = release.getProjectName();
            int progressiveNumber = release.getProgressiveNumber();

            for (Class c : classes) {
                StringBuilder sb = new StringBuilder();

                sb.append(projectName).append(",");
                sb.append(c.getName()).append(",");
                sb.append(progressiveNumber).append(",");

                sb.append(c.getLOC()).append(",");
                sb.append(c.getNumRevisions()).append(",");
                sb.append(c.getNumRevisionsFromBegin()).append(",");
                sb.append(c.getNumFixes()).append(",");
                sb.append(c.getNumFixesFromBegin()).append(",");
                sb.append(c.getNumAuthors()).append(",");
                sb.append(c.getNumAuthorsFromBegin()).append(",");
                sb.append(c.getChurn()).append(",");
                sb.append(c.getChurnFromBegin()).append(",");
                sb.append(c.getMaxLOCAdded()).append(",");
                sb.append(c.getMaxLOCAddedFromBegin()).append(",");
                sb.append(c.getAvgLOCAdded()).append(",");
                sb.append(c.getAvgLOCAddedFromBegin()).append(",");
                sb.append(c.getAvgChangeSet()).append(",");
                sb.append(c.getAvgChangeSetFromBegin()).append(",");
                sb.append(c.getMaxChangeSet()).append(",");
                sb.append(c.getMaxChangeSetFromBegin()).append(",");
                sb.append(c.getAge()).append(",");
                sb.append(c.getWeightedAge()).append(",");

                sb.append(c.getNumOps()).append(",");
                sb.append(c.getAvgTimeBetweenCommits()).append(",");
                sb.append(c.getNumSmells()).append(",");

                sb.append(c.isBuggy());

                pw.println(sb.toString());
            }

        } catch (IOException e) {
            throw new PersistenceException("Error while writing release to dataset file: " + e.getMessage());
        }
    }

    public static void writeWhichDataset(List<Release> releases, DataType type) throws PersistenceException {

        String filename;
        switch (type){
            case B_PLUS -> filename = DATASET_B_PLUS;
            case C -> filename = DATASET_C;
            case B -> filename = DATASET_B;
            default -> throw new PersistenceException("Invalid DataType: " + type);
        }

        for (Release rel : releases) {
            writeUpdateRelease(rel,filename);
        }
    }

}