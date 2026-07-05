package entity;

import settings.Balancing;
import settings.FeatureSelection;
import settings.Validation;

public class Experiment{

    private FeatureSelection featureSelection;
    private Balancing balancing;
    private Validation validation;

    public Experiment(FeatureSelection sel, Balancing bal, Validation val){
        this.featureSelection = sel;
        this.balancing = bal;
        this.validation = val;
    }

}

