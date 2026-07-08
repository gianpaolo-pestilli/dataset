package control;

import java.time.LocalDate;

public class ControlParams {
    private int added;
    private int deleted;
    private int diffSize;
    private boolean isFix;
    private String authorEmail;
    private LocalDate commitDate;
    private LocalDate projectStartDate;

    public ControlParams(int added, int deleted, int diffSize, boolean isFix, String authorEmail, LocalDate commitDate, LocalDate projectStartDate) {
        this.added = added;
        this.deleted = deleted;
        this.diffSize = diffSize;
        this.isFix = isFix;
        this.authorEmail = authorEmail;
        this.commitDate = commitDate;
        this.projectStartDate = projectStartDate;
    }

    // Getter e Setter
    public int getAdded() { return added; }
    public void setAdded(int added) { this.added = added; }

    public int getDeleted() { return deleted; }
    public void setDeleted(int deleted) { this.deleted = deleted; }

    public int getDiffSize() { return diffSize; }
    public void setDiffSize(int diffSize) { this.diffSize = diffSize; }

    public boolean isFix() { return isFix; }
    public void setFix(boolean fix) { isFix = fix; }

    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public LocalDate getCommitDate() { return commitDate; }
    public void setCommitDate(LocalDate commitDate) { this.commitDate = commitDate; }

    public LocalDate getProjectStartDate() { return projectStartDate; }
    public void setProjectStartDate(LocalDate projectStartDate) { this.projectStartDate = projectStartDate; }
}