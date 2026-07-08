package entity;

import exception.VersionException;

import java.util.List;

public class Ticket {

    public Ticket(List<String> classes){
        this.affectedClasses = classes;
    }

    public static int numInconsistentIV = 0;
    public static int notExistingBuggy = 0;

    private List<String> affectedClasses;

    private boolean consistent;

    private int OV;
    private int IV;
    private int FV;

    private boolean valid = true;

    // To evaluate PROPORTION
    private static double proportionIncrement;
    private static int numberProportions;

    private void incrementProportion(){
        if(consistent){
            double num = (double) FV - IV;
            double den = (double) FV - OV;
            double proportion = num/den;
            proportionIncrement += proportion;
            numberProportions++;
        }
    }


    // Call only after Proportion evaluation
    public void estimateIV(double proportion){
        if(consistent || !valid){
            return; // Nothing to do
        }
        // If not consistent
        int firstTerm = FV;
        double second = (FV - OV)*proportion;
        int secondTerm = (int)Math.round(second);
        this.IV = firstTerm - secondTerm;
        if (this.OV < this.IV){
            this.IV = this.OV;
        }
    }

    // Call this only at the end !!! TOTAL APPROACH
    public static double evaluateProportion(){

        if(numberProportions == 0){return 0;}

        return proportionIncrement/numberProportions;
    }


    public void setVersions(int IV, int OV, int FV)  {

        this.FV = FV; // The only thing we know
        this.OV = OV;



        if(FV <= OV){
            // we want to think that programmer wanted to open the ticket at the IV
            if((IV == -1) || (IV >= FV)){
                // This bug doesn't exist
                this.valid = false;
                notExistingBuggy++;
                return;
            } else {
                this.OV = IV;
            }
        }

        // Now that we know the bug exists
        if((IV == -1) || (this.OV < IV)){
            // Can't rely on IV
            this.consistent = false;
            numInconsistentIV++;
        } else {
            this.consistent = true;
            this.IV = IV;
            incrementProportion();
        }
    }

    public List<String> getAffectedClasses(){
        return this.affectedClasses;
    }

    public int getIV(){
        return this.IV;
    }

    public int getFV() {
        return this.FV;
    }

    public static int getInconsistentTickets(){
        return numInconsistentIV;
    }

    public static int getInexisting(){
        return notExistingBuggy;
    }

    public boolean isValid(){return valid;}
}
