package entity;

// This represents a single instance of the dataset
public class Class {

    public Class(String name, int numSmells){
        this.className = name;
        this.numSmells = numSmells;
    }

    public Class(Release release, String name){
        this.className = name;
        this.release = release;
        this.age = release.getAge();
    }

    private String className;

    private Release release;



    // Features

    private long LOC;
    private long LOCFromBegin;

    private long numRevisions;
    private long numRevisionsFromBegin;

    private long numFixes;
    private long numFixesFromBegin;

    private long numAuthors;
    private long numAuthorsFromBegin;

    private long churn;
    private long churnFromBegin;

    private long maxLOCAdded; //The highest in this release
    private long maxLOCAddedFromBegin; //The highest in the whole project until this release (included)

    private double avgLOCAdded; //The average number of LOC added per commit in this release
    private double avgLOCAddedFromBegin; //The average number of LOC added per commit from the beginning of the project

    private double avgChangeSet;
    private double avgChangeSetFromBegin;

    private long age; //Of the release
    private double weightedAge; //By LOC

    // My metrics
    private int numOps;
    private double avgTimeBetweenCommits; // In seconds

    private long numSmells;

    private boolean isBuggy;

    public String getProjectName(){
        return this.release.getProjectName();
    }

    public String getName(){
        return className;
    }

    public String getReleaseID(){
        return this.release.getID();
    }

    public double setWeightedAge(){
        return (age / churn);
    }

    public void setNumSmells(long num){
        this.numSmells = num;
    }

    public void setNumOps(int num){
        this.numOps = num;
    }

    public int getNumber(){
        return this.release.getProgressiveNumber();
    }

    public int getNumOps() {
        return numOps;
    }
    public int getNumSmells(){
        return Math.toIntExact(numSmells);
    }
}
