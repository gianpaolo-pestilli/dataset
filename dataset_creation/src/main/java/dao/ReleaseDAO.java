package dao;

import bean.ReleaseBean;
import exception.PersistenceException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ReleaseDAO {
    private static final String allReleasesFilename = "considered_releases.csv";
    private static final String firstReleasesFilename = "first_releases.csv";

    public static String getAllReleasesFilename(){
        return allReleasesFilename;
    }
    public static String getFirstReleasesFilename(){return firstReleasesFilename;}

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

}
