package entity;

import java.util.HashSet;
import java.util.Set;


public class ClassTracker {
    private int numRev = 0;
    private int numFix = 0;
    private Set<String> authors = new HashSet<>();
    private int churn = 0;
    private int maxLocAdded = 0;
    private int totalLocAdded = 0;
    private int totalLocDeleted = 0;
    private int totalChangeSet = 0;
    private int maxChangeSet = 0;

    public void incrementNumRev() { this.numRev++; }

    public void incrementNumFix() { this.numFix++; }

    public void addAuthor(String authorEmail) { this.authors.add(authorEmail); }

    public void addChurn(int added, int deleted) {
        this.churn += (added + deleted);
        this.totalLocAdded += added;
        this.totalLocDeleted += deleted;
        this.maxLocAdded = Math.max(this.maxLocAdded, added);
    }

    public void addChangeSet(int size) {
        this.totalChangeSet += size;
        this.maxChangeSet = Math.max(this.maxChangeSet, size);
    }

    public int getNumRev() { return numRev; }
    public int getNumFix() { return numFix; }
    public int getAuthorsCount() { return authors.size(); }
    public int getChurn() { return churn; }
    public int getMaxLocAdded() { return maxLocAdded; }
    public int getTotalLocAdded() { return totalLocAdded; }
    public int getTotalLocDeleted() { return totalLocDeleted; }
    public int getTotalChangeSet() { return totalChangeSet; }
    public int getMaxChangeSet() { return maxChangeSet; }
}