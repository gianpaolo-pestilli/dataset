package bean;

import java.time.LocalDate;

public class ReleaseBean {
    private String projectName;
    private LocalDate releaseDate;
    private String releaseID;
    private String version;

    public ReleaseBean(String version){
        this.version = version;
    }
    public ReleaseBean(String projectName,
                       LocalDate releaseDate,
                       String releaseID,
                       String version){
        this.projectName = projectName;
        this.releaseDate = releaseDate;
        this.releaseID = releaseID;
        this.version = version;
    }

    public String getVersion(){
        return version;
    }
    public String getID(){
        return releaseID;
    }
    public String getProjectName(){
        return projectName;
    }
    public LocalDate getReleaseDate(){
        return releaseDate;
    }

}
