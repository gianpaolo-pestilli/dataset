package model;

// This represents a single instance of the dataset
public class Class {

    public Class(String projectName){
        this.projectName = projectName;
    }


    private String projectName;

    private String className;

    private String releaseID;

    // Class metrics
    private int currentLOC;


    private int currentLOCTouched;

    //This is since the first release
    private int allLOCTouched;

    private int currentNumRevisions;

    // This is since the first release
    private int allNumRevisions;

    private int currentNumDefectFixed;

    // This is since the first release
    private int allNumDefectFixed;

    private int currentNumAuth;

    private int allNumAuth;


    private int numSmells;
    // To label
    private boolean isBuggy;

}
