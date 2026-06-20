package bean;

public class ProjectInfoBean {
    private String projectKey;
    private String projectName;
    private String releaseVersion;
    private String projectOwner;
    private String projectRepo;

    private String localPath;
    private String token;

    public ProjectInfoBean(){}

    public ProjectInfoBean(String projectKey,
                           String projectName,
                           String releaseVersion,
                           String projectOwner,
                           String projectRepo){
        this.projectKey = projectKey;
        this.projectName = projectName;
        this.releaseVersion = releaseVersion;
        this.projectOwner = projectOwner;
        this.projectRepo = projectRepo;
    }

    public String getProjectName(){
        return projectName;
    }

    public String getProjectKey(){
        return projectKey;
    }

    public String getReleaseVersion(){
        return releaseVersion;
    }

    public String getProjectOwner(){return projectOwner;}

    public String getProjectRepo(){return projectRepo;}

    public void setReleaseVersion(String release){
        this.releaseVersion = release; // Git tag without project name
    }

    public String getTag(){
        String name = getProjectName();
        String version = getReleaseVersion();
        name = name.toLowerCase();
        String output = name + "-" + version;
        return output;
    }

    public void setLocalPath(String path){
        this.localPath = path;
    }

    public void setToken(String token){
        this.token = token;
    }

    public String getLocalPath(){
        return localPath;
    }

    public String getToken(){
        return token;
    }

}
