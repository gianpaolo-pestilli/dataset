package bean;
import java.time.LocalDate;
import java.util.List;

public class CommitBean {
    private String ticketId;
    private LocalDate date;
    private List<String> touchedClasses;

    public CommitBean(String ticketId, LocalDate date, List<String> touchedClasses) {
        this.ticketId = ticketId;
        this.date = date;
        this.touchedClasses = touchedClasses;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<String> getTouchedClasses() {
        return touchedClasses;
    }

    public void setTouchedClasses(List<String> touchedClasses) {
        this.touchedClasses = touchedClasses;
    }
}
