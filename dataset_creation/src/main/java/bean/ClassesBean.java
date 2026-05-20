package bean;

public class ClassesBean {

    private String projectName;
    private String className;
    private String releaseVersion;
    private Integer numSmell;
    private Integer timeSmell; // in minutes
    private Double debt; // timeSmell scaled by the size (SonarCloud definition)

    public ClassesBean(){}

    public ClassesBean(String projectName,
                       String className,
                       String releaseVersion,
                       Integer numSmell,
                       Integer timeSmell, /*in minutes*/
                       Double debt
    ) {
        this.projectName = projectName;
        this.className = className;
        this.releaseVersion = releaseVersion;
        this.numSmell = numSmell;
        this.timeSmell = timeSmell;
        this.debt = debt;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseID(String releaseID) {
        this.releaseVersion = releaseID;
    }

    public Integer getNumSmell() {
        return numSmell;
    }

    public void setNumSmell(Integer numSmell) {
        this.numSmell = numSmell;
    }

    public Integer getTimeSmell() {
        return timeSmell;
    }

    public void setTimeSmell(Integer timeSmell) {
        this.timeSmell = timeSmell;
    }

    public Double getDebt() {
        return debt;
    }

    public void setDebt(Double debt) {
        this.debt = debt;
    }

}
