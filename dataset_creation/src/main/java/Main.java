import boundary.JiraInteraction;

public class Main {

    public static void main(String[] args) {
        try{
            JiraInteraction.printTicket();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // ApplicationStarter.launch();
    }
}
