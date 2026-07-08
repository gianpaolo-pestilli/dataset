package entity;

import settings.ClassifierName;


public class Classifier {
    private ClassifierName name;
    private double precision;
    private double recall;
    private double auc;
    private double kappa;
    private double accuracy;
    private Experiment experiment;

    private long tp;
    private long fp;
    private long tn;
    private long fn;




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

    public double getAuc() {
        return auc;
    }

    public void setAuc(double auc) {
        this.auc = auc;
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

    public long getTp() {
        return tp;
    }

    public void setTp(long tp) {
        this.tp = tp;
    }

    public long getFp() {
        return fp;
    }

    public void setFp(long fp) {
        this.fp = fp;
    }

    public long getTn() {
        return tn;
    }

    public void setTn(long tn) {
        this.tn = tn;
    }

    public long getFn() {
        return fn;
    }

    public void setFn(long fn) {
        this.fn = fn;
    }





}
