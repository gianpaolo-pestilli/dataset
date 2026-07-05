package entity;

import settings.Balancing;
import settings.FeatureSelection;
import settings.ManualCut;
import settings.Validation;

public class Experiment{

    private FeatureSelection featureSelection;
    private Balancing balancing;
    private Validation validation;
    private ManualCut cut;

    public Experiment(FeatureSelection sel, Balancing bal, Validation val, ManualCut cut){
        this.featureSelection = sel;
        this.balancing = bal;
        this.validation = val;
        this.cut = cut;
    }
    public FeatureSelection getFeatureSelection() {
        return featureSelection;
    }

    public Balancing getBalancing() {
        return balancing;
    }

    public Validation getValidation() {
        return validation;
    }

    public ManualCut getCut() {
        return cut;
    }


}

