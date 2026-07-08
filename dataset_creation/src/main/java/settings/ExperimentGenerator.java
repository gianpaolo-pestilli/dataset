package settings;

import entity.Classifier;
import entity.Experiment;

import java.util.ArrayList;
import java.util.List;

public class ExperimentGenerator {

    private ExperimentGenerator(){
        // Making it private
    }
    public static List<Classifier> generateClassifiers() {
        List<Classifier> toRet = new ArrayList<>();

        for(ManualCut c : ManualCut.values()){
            for(FeatureSelection selection : FeatureSelection.values()){
                for(Balancing bal : Balancing.values()){
                    for(Validation val : Validation.values()){
                        Experiment exp = new Experiment(selection,bal,val,c);
                            for(ClassifierName classifier : ClassifierName.values()){
                            toRet.add(new Classifier(classifier,exp));
                        }
                    }
                }
            }
        }
        return toRet;
    }
}
