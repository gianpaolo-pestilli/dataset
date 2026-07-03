package entity;

import exception.VersionException;

import java.time.LocalDate;
import java.util.List;

public class Ticket {

    public Ticket(LocalDate creationDate, List<String> classes){
        this.creationDate = creationDate;
        this.affectedClasses = classes;
    }

    // This is a buggy ticket
    private LocalDate creationDate; // To understand the opening version

    private List<String> affectedClasses;

    private boolean consistent;
    private boolean hasCommit; // If not, this ticket doesn't exist

    private int OV;
    private int IV;
    private int FV;

    // To evaluate PROPORTION
    private static double proportionIncrement;
    private static int numberProportions;

    private void incrementProportion(){
        if(consistent){
            double num = FV-IV;
            double den = FV-OV;
            double proportion = num/den;
            proportionIncrement += proportion;
            numberProportions++;
        }
    }


    // Call only after Proportion evaluation
    public void estimateIV(double proportion){
        if(consistent){
            return; // Nothing to do
        }
        // If not consistent
        int firstTerm = FV;
        double second = (FV - OV)*proportion;
        int secondTerm = (int)Math.round(second);
        this.IV = firstTerm - secondTerm;

    }

    // Call this only at the end !!! TOTAL APPROACH
    public static double evaluateProportion(){
        return proportionIncrement/numberProportions;
    }

    public void setCommit(boolean b){
        this.hasCommit = b;
        if(!b){
            this.consistent = false;
        }
    }

    public void setVersions(int IV, int OV, int FV) throws VersionException {
        if(FV <= OV){
            // Fixed at the same release you opened... Something is not correct
            throw new VersionException("FV = OV");
        }
        this.OV = OV;
        this.FV = FV;
        if(IV <= OV){
            this.consistent = true;
            this.IV = IV;
        }else{
            this.consistent = false;
        }
        incrementProportion();
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
}
