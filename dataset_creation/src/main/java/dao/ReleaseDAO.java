package dao;

import bean.ReleaseBean;
import entity.Release;
import exception.PersistenceException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReleaseDAO {
    private static final String ALL_RELEASES_FILENAME = "considered_releases.csv";
    private static final String FIRST_RELEASES_FILENAME = "first_releases.csv";

    public static String getAllReleasesFilename(){
        return ALL_RELEASES_FILENAME;
    }
    public static String getFirstReleasesFilename(){return FIRST_RELEASES_FILENAME;}

    // Not the best way to call a DAO, but I don't want a DTO which is equal to an already defined Bean
    public static void writeAllReleases(List<ReleaseBean> releases) throws PersistenceException {
        try (FileWriter writer = new FileWriter(getAllReleasesFilename())) {
            // Write header
            writer.append("Index,Project Name,Version,Release ID,Release Date\n");

            // Write data
            int i = 1;
            for (ReleaseBean release : releases) {
                writer.append(String.valueOf(i));
                writer.append(',');
                writer.append(release.getProjectName());
                writer.append(',');
                writer.append(release.getVersion());
                writer.append(',');
                writer.append(release.getID());
                writer.append(',');
                writer.append(release.getReleaseDate().toString());
                writer.append('\n');
                i++;
            }
        } catch (IOException e) {
            throw new PersistenceException("Error while writing releases to file: " + e.getMessage());
        }
    }

    public static void writeFirstReleases(List<ReleaseBean> releases) throws PersistenceException {
        try (FileWriter writer = new FileWriter(getFirstReleasesFilename())) {
            // Write header
            writer.append("Index,Project Name,Version,Release ID,Release Date\n");

            // Write data
            int i = 1;
            for (ReleaseBean release : releases) {
                writer.append(String.valueOf(i));
                writer.append(',');
                writer.append(release.getProjectName());
                writer.append(',');
                writer.append(release.getVersion());
                writer.append(',');
                writer.append(release.getID());
                writer.append(',');
                writer.append(release.getReleaseDate().toString());
                writer.append('\n');
                i++;
            }
        } catch (IOException e) {
            throw new PersistenceException("Error while writing releases to file: " + e.getMessage());
        }
    }

    public static List<Release> getVeryFirstReleases() throws PersistenceException {
        // Return the first 33% of the releases (from first_releases.csv)
        List<Release> releases = new ArrayList<>();
        String csvFile = FIRST_RELEASES_FILENAME;
        String line;
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Assicuriamoci che il file non sia vuoto prima di scartare l'intestazione
            String header = br.readLine();
            if (header == null) {
                return releases;
            }

            while ((line = br.readLine()) != null) {
                String[] data = line.split(csvSplitBy);
                if (data.length >= 5) {
                    Release release = getRelease(data);
                    releases.add(release);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Error occurred while reading file: " + csvFile +"\n"+e.getMessage());
        }
        return releases;
    }

    private static Release getRelease(String[] data) {
        int progressiveNumber = Integer.parseInt(data[0].trim());
        String projectName = data[1].trim();
        String tag = data[2].trim();
        String JiraID = data[3].trim();
        String releaseDateStr = data[4].trim();
        LocalDate releaseDate = LocalDate.parse(releaseDateStr);
        Release release = new Release(projectName,releaseDate,JiraID,tag);
        release.setProgressiveNumber(progressiveNumber);
        return release;
    }

    public static List<Release> getEveryRelease() throws PersistenceException {
        List<Release> releases = new ArrayList<>();
        String csvFile = ALL_RELEASES_FILENAME;
        String line;
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Assicuriamoci che il file non sia vuoto prima di scartare l'intestazione
            String header = br.readLine();
            if (header == null) {
                return releases;
            }

            while ((line = br.readLine()) != null) {
                String[] data = line.split(csvSplitBy);
                if (data.length >= 5) {
                    Release release = getRelease(data);
                    releases.add(release);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Error occurred while reading file: " + csvFile +"\n"+e.getMessage());
        }
        return releases;
    }

    public static int getMaxID() throws PersistenceException{
        int maxID = 0;
        String csvFile = FIRST_RELEASES_FILENAME;
        String line;
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Assicuriamoci che il file non sia vuoto prima di scartare l'intestazione
            String header = br.readLine();
            if (header == null) {
                return maxID;
            }

            while ((line = br.readLine()) != null) {
                String[] data = line.split(csvSplitBy);
                if (data.length >= 5) {
                    int num = Integer.parseInt(data[0].trim());
                    if (num > maxID){maxID = num;}
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Error occurred while reading file: " + csvFile +"\n"+e.getMessage());
        }
        return maxID;
    }

}