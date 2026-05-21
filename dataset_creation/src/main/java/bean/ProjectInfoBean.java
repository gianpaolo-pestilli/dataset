package bean;

public class ProjectInfoBean {
    private String projectKey;
    private String projectName;
    private String releaseVersion;
    private String projectOwner;
    private String projectRepo;

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

}
