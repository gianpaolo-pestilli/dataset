package bean;

import entity.Ticket;

import java.time.LocalDate;
import java.util.List;

public class TicketBean {

    public TicketBean(int FVreleaseID,
                      List<Integer> affectedversions,
                      LocalDate creation,
                      List<String> classes){
        this.FVreleaseID = FVreleaseID;
        this.allAffectedVersionsID = affectedversions;
        this.creationDate = creation;

    }

    private int FVreleaseID;

    private List<Integer> allAffectedVersionsID;

    private LocalDate creationDate; //For the OV

    private List<String> affectedClasses;




}
