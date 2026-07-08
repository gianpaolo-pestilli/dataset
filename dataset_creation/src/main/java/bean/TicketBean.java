package bean;

import java.time.LocalDate;
import java.util.List;

public class TicketBean {

    private String ticketID;
    private LocalDate creationDate;
    private List<String> allAffectedVersionsID;

    private int fvReleaseID;
    private List<String> affectedClasses;

    public TicketBean(String ticketID, LocalDate creationDate, List<String> allAffectedVersionsID) {
        this.ticketID = ticketID;
        this.creationDate = creationDate;
        this.allAffectedVersionsID = allAffectedVersionsID;
    }

    public String getTicketID() { return ticketID; }
    public LocalDate getCreationDate() { return creationDate; }
    public List<String> getAllAffectedVersionsID() { return allAffectedVersionsID; }

    public void setFvReleaseID(int fvReleaseID) { this.fvReleaseID = fvReleaseID; }
    public int getFvReleaseID() { return fvReleaseID; }

    public void setAffectedClasses(List<String> affectedClasses) { this.affectedClasses = affectedClasses; }
    public List<String> getAffectedClasses() { return affectedClasses; }
}