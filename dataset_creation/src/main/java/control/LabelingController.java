package control;

import bean.*;
import boundary.api.GitInteraction;
import boundary.api.JiraInteraction;
import dao.DatasetDAO;
import dao.ProportionDAO;
import dao.ReleaseDAO;
import dao.dto.TicketDTO;
import entity.Class;
import entity.LabelClass;
import entity.Release;
import entity.Ticket;
import exception.*;
import settings.PropertiesSetter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LabelingController extends AppController{

    private List<ReleaseBean> allJiraReleases = new ArrayList<>(); //Every release from Jira, phantoms too

    private List<Release> allConsideredReleases = new ArrayList<>(); // 100% of filtered releases

    private List<Ticket> allTickets = new ArrayList<>(); // All consistent and not consistent tickets

    private List<CommitBean> allCommits = new ArrayList<>();

    private double proportion;

    private List<Release> toLabel = new ArrayList<>();

    private double howManyInconsistent;
    private double howManyFake;
    private double ticketsWithNoCommit;

    @Override
    public void start() throws ControllerException{

        String projectName;
        String owner;
        String repo;
        String path;
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
            path = PropertiesSetter.getProjectLocalPath();
            param.setLocalPath(path);

            this.allJiraReleases = JiraInteraction.getAllReleases(param);

            this.allConsideredReleases = ReleaseDAO.getEveryRelease();

            this.toLabel = DatasetDAO.getDataset(); // Dataset in RAM

            List<TicketBean> ticketFromJira = JiraInteraction.getAllTickets(param);

            this.allCommits = GitInteraction.extractAllCommits(param);

            filterTicket(param,ticketFromJira);

            this.proportion = Ticket.evaluateProportion();

            userBoundary.printMessage(new MessageBean("Evaluated proportion = " + this.proportion));

            doLabeling();

        } catch (ConfigException|JiraException|PersistenceException|GitException e) {
            throw new ControllerException("Error: " + e.getMessage());
        }

    }

    @Override
    public void finish() throws ControllerException {
        try{
            double percentage = evalPercentage();

            ProportionDAO.writeProportion(this.proportion);
            ProportionDAO.writePercentage(percentage);
            ProportionDAO.writeTicketWithNoCommit(this.ticketsWithNoCommit);
            ProportionDAO.writeFake(this.howManyFake);
            ProportionDAO.writeInconsistent(this.howManyInconsistent);

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

    private void filterTicket(ProjectInfoBean info, List<TicketBean> tickets) throws ControllerException{
        try {
            // If a ticket has no commits we discard it
            Set<String> ticketTrovatiSuGit = GitInteraction.getBuggyMessageID(info);
            double initialSize = tickets.size(); // 1. Salviamo la cardinalità iniziale
            tickets.removeIf(ticket -> !ticketTrovatiSuGit.contains(ticket.getTicketID())); // 2. Rimuoviamo
            this.ticketsWithNoCommit = (initialSize - tickets.size())/(initialSize);

            // We have only committed tickets

            for(TicketBean tick : tickets){
                int ov = getOpening(tick);
                if(ov != -1){ // Not ignored
                    int possibleIV = getPossibleInjected(tick);
                    TicketDTO t = getFixed(tick);
                    int fv = t.index();
                    if(fv != -1){
                        List<String> affectedClasses = t.words();
                        Ticket ticket = new Ticket(affectedClasses);
                        ticket.setVersions(possibleIV,ov,fv);
                        allTickets.add(ticket);
                    }
                }
            }
            this.howManyFake = Ticket.getInexisting();
            this.howManyInconsistent = Ticket.getInconsistentTickets();
            double card = allTickets.size();

            this.howManyFake = howManyFake/card;
            this.howManyInconsistent = howManyInconsistent/card;

            List<Ticket> valid = new ArrayList<>();
            for(Ticket t: allTickets){
                if(t.isValid()){
                    valid.add(t);
                }
            }
            allTickets = valid;

        } catch (GitException e) {
            throw new ControllerException(e.getMessage());
        }
    }

    private void doLabeling() throws ControllerException{
        List<LabelClass> classes = new ArrayList<>();
        for(Ticket t: this.allTickets){
            t.estimateIV(this.proportion);
            int first = t.getIV();
            int last = t.getFV()-1; // Last one is not buggy
            for(String classname : t.getAffectedClasses()){
                LabelClass newClass = new LabelClass(classname, first, last);
                classes.add(newClass);
            }
        }


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

    int getOpening(TicketBean t) {
        LocalDate l = t.getCreationDate();

        // Protezione di base
        if (allConsideredReleases == null || allConsideredReleases.isEmpty()) {
            return -1;
        }

        // Regola 3: Ticket aperto prima della primissima release nota -> Ignoro
        if (l.isBefore(allConsideredReleases.get(0).getReleaseDate())) {
            return -1;
        }

        // Regola 1: Finestra temporale tra una release e la successiva
        for (int i = 0; i < allConsideredReleases.size() - 1; i++) {
            LocalDate first = allConsideredReleases.get(i).getReleaseDate();
            LocalDate second = allConsideredReleases.get(i + 1).getReleaseDate();

            // Se la data è >= first e strettamente < second
            if (!l.isBefore(first) && l.isBefore(second)) {
                return allConsideredReleases.get(i).getProgressiveNumber();
            }
        }

        // Regola 2: Ticket aperto dopo l'ultima release a disposizione -> Ignoro
        return -1;
    }

    public int getPossibleInjected(TicketBean ticket) {
        List<String> affectedIds = ticket.getAllAffectedVersionsID();

        // Nessuna AV da Jira -> ci penserà il Proportion
        if (affectedIds == null || affectedIds.isEmpty()) {
            return -1;
        }

        // 1. e 2. Recupero le date da Jira
        List<LocalDate> affectedDates = new ArrayList<>();
        for (String id : affectedIds) {
            for (bean.ReleaseBean jiraRel : allJiraReleases) {
                if (jiraRel.getID().equals(id) && jiraRel.getReleaseDate() != null) {
                    affectedDates.add(jiraRel.getReleaseDate());
                    break;
                }
            }
        }

        if (affectedDates.isEmpty()) {
            return -1;
        }

        // 3. Prendo la data più vecchia dell'infezione (aggiornato per evitare fully-qualified name)
        affectedDates.sort(Comparator.naturalOrder());
        LocalDate oldestDate = affectedDates.get(0);

        if (allConsideredReleases == null || allConsideredReleases.isEmpty()) {
            return -1;
        }

        // 4. Mappatura esatta o successiva (Regola di Y)
        for (Release release : allConsideredReleases) {
            // !release.getReleaseDate().isBefore(oldestDate) equivale a: releaseDate >= oldestDate
            if (!release.getReleaseDate().isBefore(oldestDate)) {
                return release.getProgressiveNumber();
            }
        }

        return -1;
    }

    public TicketDTO getFixed(TicketBean ticket) {
        // 1. Filtro i commit che appartengono a questo ticket
        List<CommitBean> commitsPerTicket = new ArrayList<>();
        int version = -1;

        for (CommitBean cb : this.allCommits) {
            if (ticket.getTicketID().equals(cb.getTicketId())) {
                commitsPerTicket.add(cb);
            }
        }

        if (commitsPerTicket.isEmpty()) {
            version = -1;
        }

        // 2. Aggrego classi (Set per evitare duplicati) e trovo data più recente
        Set<String> allAffectedClasses = new HashSet<>();
        LocalDate latestDate = null;

        for (CommitBean cb : commitsPerTicket) {
            allAffectedClasses.addAll(cb.getTouchedClasses());

            if (latestDate == null || cb.getDate().isAfter(latestDate)) {
                latestDate = cb.getDate();
            }
        }

        // 3. Aggiorno il TicketBean con le informazioni trovate
        // Assumo che TicketBean abbia i metodi set per queste informazioni

        List<String> affected = new ArrayList<>(allAffectedClasses);

        if (allConsideredReleases == null || allConsideredReleases.isEmpty()) {
            version = -1;
        }

        for (Release release : allConsideredReleases) {
            if (!release.getReleaseDate().isBefore(latestDate)) {
                version = release.getProgressiveNumber();
                break;
            }
        }

        return new TicketDTO(version,affected);
    }

    private double evalPercentage(){
        // Utilizzo di int per i contatori come richiesto da Sonar, per evitare perdite di precisione
        int buggyClasses = 0;
        int allClasses = 0;

        for(Release r : toLabel){
            for(Class c : r.getClasses()){
                allClasses++;
                if(c.isBuggy()){
                    buggyClasses++;
                }
            }
        }

        // Divisione finale convertita in double per restituire la percentuale
        return allClasses == 0 ? 0.0 : (double) buggyClasses / allClasses;
    }
}