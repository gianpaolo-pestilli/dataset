package dao;

import bean.ReleaseBean;
import exception.PersistenceException;

import java.util.List;

public class ReleaseDAO {
    private static final String filename = "considered_releases.csv";


    // Not the best way to call a DAO, but I don't want a DTO which is equal to an already defined Bean
    public static void writeReleases(List<ReleaseBean> releases) throws PersistenceException {


    }
}
