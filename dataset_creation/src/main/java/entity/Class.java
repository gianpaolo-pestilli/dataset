package entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

// This represents a single instance of the dataset
public class Class {

    public Class(String name, int numSmells){
        this.className = name;
        this.numSmells = numSmells;
    }

    public Class(Release release, String name){
        this.className = name;
        this.release = release;
    }

    public Class(Release release, String name, int numSmells, int numOps, int LOC){
        this.className = name;
        this.release = release;
        this.numSmells = numSmells;
        this.numOps = numOps;
        this.LOC = LOC;
    }

    private String className;
    private Release release;

    // --- Features Esistenti ---
    private long LOC;

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

    private double maxChangeSet;
    private double maxChangeSetFromBegin;

    private double weightedAge; //By LOC

    // My metrics
    private int numOps;
    private double avgTimeBetweenCommits; // Utilizzato per la Metrica 20 (Frequenza)

    private long numSmells;
    private boolean isBuggy;

    // --- Variabili di supporto (invisibili all'esterno, servono solo per i calcoli interni) ---
    private Set<String> authorsInRelease = new HashSet<>();
    private long totalLocAddedInRelease = 0;
    private long totalLocAddedFromBegin = 0;
    private long totalLocDeletedFromBegin = 0;
    private long totalChangeSetInRelease = 0;
    private long totalChangeSetFromBegin = 0;
    private LocalDate firstTouchedInRelease = null;
    private LocalDate lastTouchedInRelease = null;
    private double weightedAgeNumerator = 0.0;

    // ========================================================================
    // METODI DI ELABORAZIONE E POPOLAMENTO DATI (Chiamati dal Controller)
    // ========================================================================

    public void updateFromTracker(ClassTracker tracker) {
        this.numRevisionsFromBegin = tracker.getNumRev();
        this.numFixesFromBegin = tracker.getNumFix();
        this.numAuthorsFromBegin = tracker.getAuthorsCount();
        this.churnFromBegin = tracker.getChurn();
        this.maxLOCAddedFromBegin = tracker.getMaxLocAdded();

        // Metrica 1: Grandezza attuale della classe calcolata aritmeticamente
        this.totalLocAddedFromBegin = tracker.getTotalLocAdded();
        this.totalLocDeletedFromBegin = tracker.getTotalLocDeleted();
        // Medie storiche
        this.avgLOCAddedFromBegin = this.numRevisionsFromBegin == 0 ? 0.0 : (double) this.totalLocAddedFromBegin / this.numRevisionsFromBegin;

        this.totalChangeSetFromBegin = tracker.getTotalChangeSet();
        this.maxChangeSetFromBegin = tracker.getMaxChangeSet();
        this.avgChangeSetFromBegin = this.numRevisionsFromBegin == 0 ? 0.0 : (double) this.totalChangeSetFromBegin / this.numRevisionsFromBegin;
    }

    public void processCommitInWindow(int added, int deleted, int changeSetSize, boolean isFix,
                                      String author, LocalDate commitDate, LocalDate projectStartDate) {

        // Accumulatori Base
        this.numRevisions++;
        if (isFix) this.numFixes++;

        this.authorsInRelease.add(author);
        this.numAuthors = this.authorsInRelease.size();

        long commitChurn = (long) added + deleted;
        this.churn += commitChurn;

        // LOC e ChangeSet
        this.totalLocAddedInRelease += added;
        this.maxLOCAdded = Math.max(this.maxLOCAdded, added);
        this.avgLOCAdded = (double) this.totalLocAddedInRelease / this.numRevisions;

        this.totalChangeSetInRelease += changeSetSize;
        this.maxChangeSet = Math.max(this.maxChangeSet, changeSetSize);
        this.avgChangeSet = (double) this.totalChangeSetInRelease / this.numRevisions;

        // Frequenza (Metrica 20)
        if (this.firstTouchedInRelease == null || commitDate.isBefore(this.firstTouchedInRelease)) {
            this.firstTouchedInRelease = commitDate;
        }
        if (this.lastTouchedInRelease == null || commitDate.isAfter(this.lastTouchedInRelease)) {
            this.lastTouchedInRelease = commitDate;
        }

        long daysBetween = ChronoUnit.DAYS.between(this.firstTouchedInRelease, this.lastTouchedInRelease);
        this.avgTimeBetweenCommits = daysBetween == 0 ? (double) this.numRevisions : (double) this.numRevisions / daysBetween;

        // Weighted Age (Formula di Moser: Somma(Age_in_weeks * Churn) / Churn Totale)
        long commitAgeInWeeks = 0;
        if (projectStartDate != null) {
            commitAgeInWeeks = Math.max(0, ChronoUnit.WEEKS.between(projectStartDate, commitDate));
        }
        this.weightedAgeNumerator += (commitAgeInWeeks * commitChurn);
        this.weightedAge = this.churn == 0 ? 0.0 : this.weightedAgeNumerator / this.churn;
    }

    // ========================================================================
    // TUTTI I GETTER E SETTER (Nessun buco lasciato)
    // ========================================================================

    public String getProjectName(){
        return this.release.getProjectName();
    }

    public String getName(){
        return className;
    }

    public String getReleaseID(){
        return this.release.getID();
    }

    public int getNumber(){
        return this.release.getProgressiveNumber();
    }

    // --- Smells, Ops e Buggy ---

    public int getNumSmells(){
        return Math.toIntExact(numSmells);
    }

    public void setNumSmells(long num){
        this.numSmells = num;
    }

    public int getNumOps() {
        return numOps;
    }

    public void setNumOps(int num){
        this.numOps = num;
    }

    public boolean isBuggy() {
        return isBuggy;
    }

    public void setBuggy(boolean buggy) {
        isBuggy = buggy;
    }

    // --- Features Getters ---

    public void setLOC(int loc){
        this.LOC = loc;
    }

    public long getLOC() {
        return LOC;
    }

    public long getNumRevisions() {
        return numRevisions;
    }

    public long getNumRevisionsFromBegin() {
        return numRevisionsFromBegin;
    }

    public long getNumFixes() {
        return numFixes;
    }

    public long getNumFixesFromBegin() {
        return numFixesFromBegin;
    }

    public long getNumAuthors() {
        return numAuthors;
    }

    public long getNumAuthorsFromBegin() {
        return numAuthorsFromBegin;
    }

    public long getChurn() {
        return churn;
    }

    public long getChurnFromBegin() {
        return churnFromBegin;
    }

    public long getMaxLOCAdded() {
        return maxLOCAdded;
    }

    public long getMaxLOCAddedFromBegin() {
        return maxLOCAddedFromBegin;
    }

    public double getAvgLOCAdded() {
        return avgLOCAdded;
    }

    public double getAvgLOCAddedFromBegin() {
        return avgLOCAddedFromBegin;
    }

    public double getAvgChangeSet() {
        return avgChangeSet;
    }

    public double getAvgChangeSetFromBegin() {
        return avgChangeSetFromBegin;
    }

    public double getMaxChangeSet() {
        return maxChangeSet;
    }

    public double getMaxChangeSetFromBegin() {
        return maxChangeSetFromBegin;
    }

    // L'ETÀ ORA VIENE PRESA DALLA RELEASE
    public long getAge() {
        return this.release != null ? this.release.getAge() : 0;
    }

    public double getWeightedAge() {
        return weightedAge;
    }

    public double getAvgTimeBetweenCommits() {
        return avgTimeBetweenCommits;
    }

    public void setNumRevisionsFromBegin(long numRevisionsFromBegin) {
        this.numRevisionsFromBegin = numRevisionsFromBegin;
    }

    public void setNumFixesFromBegin(long numFixesFromBegin) {
        this.numFixesFromBegin = numFixesFromBegin;
    }

    public void setNumAuthorsFromBegin(long numAuthorsFromBegin) {
        this.numAuthorsFromBegin = numAuthorsFromBegin;
    }

    public void setChurnFromBegin(long churnFromBegin) {
        this.churnFromBegin = churnFromBegin;
    }

    public void setMaxLOCAddedFromBegin(long maxLOCAddedFromBegin) {
        this.maxLOCAddedFromBegin = maxLOCAddedFromBegin;
    }

    public void setAvgLOCAddedFromBegin(double avgLOCAddedFromBegin) {
        this.avgLOCAddedFromBegin = avgLOCAddedFromBegin;
    }

    public void setAvgChangeSetFromBegin(double avgChangeSetFromBegin) {
        this.avgChangeSetFromBegin = avgChangeSetFromBegin;
    }

    public void setMaxChangeSetFromBegin(double maxChangeSetFromBegin) {
        this.maxChangeSetFromBegin = maxChangeSetFromBegin;
    }

    public void label(){
        this.isBuggy = true;
    }
    public void setNumRevisions(long rev){
        this.numRevisions = rev;
    }

    public void setNumFixes(long numF){
        this.numFixes = numF;
    }

    public void setNumAuthors(long auth){
        this.numAuthors = auth;
    }

    public void setChurn(long ch){
        this.churn = ch;
    }
    public void setMaxLOCAdded(long loc){
        this.maxLOCAdded = loc;
    }

    public void setAvgLOCAdded(double avg){
        this.avgLOCAdded = avg;
    }

    public void setAvgChangeSet(double d){
        this.avgChangeSet = d;
    }

    public void setMaxChangeSet(double a){
        this.maxChangeSet = a;
    }

    public void setWAge(double d){
        this.weightedAge = d;
    }
    public void setAvgTimeBetweenCommits(double time){
        this.avgTimeBetweenCommits = time;
    }

    public void setRelease(Release rel){
        this.release = rel;
    }

}