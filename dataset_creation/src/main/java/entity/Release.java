package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Release {

    private String projectName;
    private LocalDate releaseDate;
    private String releaseID;
    private String version;

    private LocalDate firstCommitDate;
    private LocalDate lastCommitDate;

    private int progressiveNumber; // Useful for Proportion

    private long age;

    private List<Class> classes = new ArrayList<>();

    public Release(String projectName, int progressiveNumber){
        this.projectName = projectName;
        this.progressiveNumber = progressiveNumber;
    }

    public Release(String projectName,
                   LocalDate releaseDate,
                   String releaseID,
                   String version){
        this.projectName = projectName;
        this.releaseDate = releaseDate;
        this.releaseID = releaseID;
        this.version = version;
    }

    public String getProjectName(){
        return projectName;
    }
    public LocalDate getReleaseDate(){
        return releaseDate;
    }
    public String getID(){
        return releaseID;
    }
    public String getVersion(){return version;}

    public void addClass(String classname){
        Class newClass = new Class(this, classname);
        classes.add(newClass);
    }

    public void addClass(String classname,int numSmell,int numOps, int loc){
        Class newClass = new Class(this, classname,numSmell,numOps,loc);
        classes.add(newClass);
    }


    public void setAge(long age){
        this.age = age;
    }

    public long getAge(){
        return age;
    }

    public List<Class> getClasses(){
        return classes;
    }

    public void setProgressiveNumber(int x){
        progressiveNumber = x;
    }

    public int getProgressiveNumber(){return progressiveNumber;}

    public void setLastCommitDate(LocalDate lastCommitDate){
        this.lastCommitDate = lastCommitDate;
    }

    public void setFirstCommitDate(LocalDate firstCommitDate){
        this.firstCommitDate = firstCommitDate;
    }

    public LocalDate getFirstCommitDate(){
        return firstCommitDate;
    }

    public LocalDate getLastCommitDate(){
        return lastCommitDate;
    }

    public void addClass(Class c){
        this.classes.add(c);
    }

    public void setClasses(List<Class> classes){

        this.classes = classes;

        //Only to be safe
        for(Class c : this.classes){
            c.setRelease(this);
        }
    }
}