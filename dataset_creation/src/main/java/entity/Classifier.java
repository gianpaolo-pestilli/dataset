package entity;

import settings.ClassifierName;
import settings.ExperimentGenerator;

import java.util.List;

public class Classifier {
    private ClassifierName name;
    private double precision;
    private double recall;
    private double AUC;
    private double kappa;
    private double accuracy;
    private Experiment experiment;

    private long TP;
    private long FP;
    private long TN;
    private long FN;




    public Classifier(ClassifierName name, Experiment exp){
        this.name = name;
        this.experiment = exp;
    }
    public ClassifierName getName() {
        return name;
    }

    public void setName(ClassifierName name) {
        this.name = name;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public double getAUC() {
        return AUC;
    }

    public void setAUC(double AUC) {
        this.AUC = AUC;
    }

    public double getKappa() {
        return kappa;
    }

    public void setKappa(double kappa) {
        this.kappa = kappa;
    }


    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public long getTP() {
        return TP;
    }

    public void setTP(long TP) {
        this.TP = TP;
    }

    public long getFP() {
        return FP;
    }

    public void setFP(long FP) {
        this.FP = FP;
    }

    public long getTN() {
        return TN;
    }

    public void setTN(long TN) {
        this.TN = TN;
    }

    public long getFN() {
        return FN;
    }

    public void setFN(long FN) {
        this.FN = FN;
    }





}
