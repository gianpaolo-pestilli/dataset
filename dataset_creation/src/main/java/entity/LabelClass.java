package entity;

import java.util.ArrayList;
import java.util.List;

public class LabelClass {
    private String classname;
    private int firstReleaseToLabel;
    private int lastReleaseToLabel; //Needs to be included

    public LabelClass(String name, int first, int last){
        this.classname = name;
        this.firstReleaseToLabel = first;
        this.lastReleaseToLabel = last;
    }

    public String getClassname(){
        return this.classname;
    }
    public int getFirstRelease(){
        return this.firstReleaseToLabel;
    }
    public int getLastRelease(){
        return this.lastReleaseToLabel;
    }

}
