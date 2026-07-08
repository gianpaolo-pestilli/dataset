package entity;

import java.util.List;

public class Ticket {

    public Ticket(List<String> classes){
        this.affectedClasses = classes;
    }

    public static int numInconsistentIV = 0;
    public static int notExistingBuggy = 0;

    private List<String> affectedClasses;

    private boolean consistent;

    private int ov;
    private int iv;
    private int fv;

    private boolean valid = true;

    // To evaluate PROPORTION
    private static double proportionIncrement;
    private static int numberProportions;

    private void incrementProportion(){
        if(consistent){
            double num = (double) fv - iv;
            double den = (double) fv - ov;
            double proportion = num/den;
            // Accesso esplicito alla classe per eliminare lo smell
            Ticket.proportionIncrement += proportion;
            Ticket.numberProportions++;
        }
    }


    // Call only after Proportion evaluation
    public void estimateIV(double proportion){
        if(consistent || !valid){
            return; // Nothing to do
        }
        // If not consistent
        int firstTerm = fv;
        double second = (fv - ov)*proportion;
        int secondTerm = (int)Math.round(second);
        this.iv = firstTerm - secondTerm;
        if (this.ov < this.iv){
            this.iv = this.ov;
        }
    }

    // Call this only at the end !!! TOTAL APPROACH
    public static double evaluateProportion(){

        if(numberProportions == 0){return 0;}

        return proportionIncrement/numberProportions;
    }


    public void setVersions(int iv, int ov, int fv)  {

        this.fv = fv; // The only thing we know
        this.ov = ov;

        if(fv <= ov){
            // we want to think that programmer wanted to open the ticket at the IV
            if((iv == -1) || (iv >= fv)){
                // This bug doesn't exist
                this.valid = false;
                // Accesso esplicito alla classe per eliminare lo smell
                Ticket.notExistingBuggy++;
                return;
            } else {
                this.ov = iv;
            }
        }

        // Now that we know the bug exists
        if((iv == -1) || (this.ov < iv)){
            // Can't rely on IV
            this.consistent = false;
            // Accesso esplicito alla classe per eliminare lo smell
            Ticket.numInconsistentIV++;
        } else {
            this.consistent = true;
            this.iv = iv;
            incrementProportion();
        }
    }

    public List<String> getAffectedClasses(){
        return this.affectedClasses;
    }

    public int getIv(){
        return this.iv;
    }

    public int getFv() {
        return this.fv;
    }

    public static int getInconsistentTickets(){
        return numInconsistentIV;
    }

    public static int getInexisting(){
        return notExistingBuggy;
    }

    public boolean isValid(){return valid;}
}