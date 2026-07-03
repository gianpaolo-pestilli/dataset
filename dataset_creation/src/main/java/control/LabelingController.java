package control;

import bean.ProjectInfoBean;
import bean.ReleaseBean;
import bean.TicketBean;
import boundary.api.JiraInteraction;
import dao.DatasetDAO;
import dao.ProportionDAO;
import dao.ReleaseDAO;
import entity.Class;
import entity.LabelClass;
import entity.Release;
import entity.Ticket;
import exception.*;
import settings.PropertiesSetter;

import java.util.ArrayList;
import java.util.List;

public class LabelingController extends AppController{

    private List<ReleaseBean> allJiraReleases = new ArrayList<>(); //Every release from Jira, phantoms too

    private List<Release> allConsideredReleases = new ArrayList<>(); // 100% of filtered releases

    private List<Ticket> allTickets = new ArrayList<>(); // All consistent and not consistent tickets

    private double proportion;

    private List<Release> toLabel = new ArrayList<>();

    @Override
    public void start() throws ControllerException{

        String projectName;
        String owner;
        String repo;
        try {
            projectName = PropertiesSetter.getProjectName();
            owner = PropertiesSetter.getOwner();
            repo = PropertiesSetter.getRepo();
            ProjectInfoBean param = new ProjectInfoBean(
                    null,
                    projectName,
                    null,
                    owner,
                    repo
            );
            this.allJiraReleases = JiraInteraction.getAllReleases(param);
            this.allConsideredReleases = ReleaseDAO.getEveryRelease();
            this.toLabel = DatasetDAO.getDataset();

            List<TicketBean> ticketFromJira = JiraInteraction.getAllTickets();
            filterTicket(ticketFromJira);

            this.proportion = Ticket.evaluateProportion();

            doLabeling();

        } catch (ConfigException|JiraException|PersistenceException e) {
            throw new ControllerException("Error: " + e.getMessage());
        }



    }

    @Override
    public void finish() throws ControllerException {
        try{
            double percentage = evalPercentage();
            ProportionDAO.writeProportion(this.proportion);
            ProportionDAO.writePercentage(percentage);
            DatasetDAO.writeFinalDataset(this.toLabel);
        }catch (PersistenceException e){
            throw new ControllerException(e.getMessage());
        }
    }

    private boolean isSameClass(String path1, String path2) {
        if (path1 == null || path2 == null) return false;
        // Estrarre solo il nome del file (es: UserDAOImpl.java) è il modo più affidabile
        // per mappare le classi quando cambiano cartelle nel tempo.
        String name1 = path1.substring(path1.lastIndexOf('/') + 1);
        String name2 = path2.substring(path2.lastIndexOf('/') + 1);
        return name1.equals(name2);
    }

    private void filterTicket(List<TicketBean> tickets){

        // this.allTickets will be a consistent set of values


    }

    private void doLabeling() throws ControllerException{
        List<LabelClass> classes = new ArrayList<>();
        for(Ticket t: this.allTickets){
            int first = t.getIV();
            int last = t.getFV()-1; // Last one is not buggy
            for(String classname : t.getAffectedClasses()){
                LabelClass newClass = new LabelClass(classname, first, last);
                classes.add(newClass);
            }
        }

        int minimum = 1;
        int max;
        try {
            max = ReleaseDAO.getMaxID();
        } catch (PersistenceException e) {
            throw new ControllerException(e.getMessage());
        }

        for(LabelClass cl : classes){
            int first = cl.getFirstRelease();
            int last = cl.getLastRelease();
            for(int i = Math.max(1,first); i <= Math.min(max,last); i++){
                int index = i-1;
                Release interestedRelease = toLabel.get(index);
                for(Class clas : interestedRelease.getClasses()){
                    if(isSameClass(clas.getName(),cl.getClassname())){
                        clas.label();
                    }
                }
            }
        }

        // In toLabel we have the labeled classes

        }


}
