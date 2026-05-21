package bean;

public class ProjectInfoBean {
    private String projectKey;
    private String projectName;
    private String releaseVersion;

    public ProjectInfoBean(){}

    public ProjectInfoBean(String projectKey, String projectName, String releaseVersion){
        this.projectKey = projectKey;
        this.projectName = projectName;
        this.releaseVersion = releaseVersion;
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

}
