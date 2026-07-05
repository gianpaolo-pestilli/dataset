package entity;

import settings.ClassifierName;
import settings.ExperimentGenerator;

public class Classifier {
    private ClassifierName name;
    private double precision;
    private double recall;
    private double AUC;
    private double kappa;
    private double NPofB20;
    private double accuracy;
    private Experiment experiment;

    public Classifier(ClassifierName name, Experiment exp){
        this.name = name;
        this.experiment = exp;
    }


}
