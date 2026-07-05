package settings;

import entity.Classifier;
import entity.Experiment;

import java.util.ArrayList;
import java.util.List;

public class ExperimentGenerator {

    public static List<Classifier> generateClassifiers() {
        List<Classifier> toRet = new ArrayList<>();
        for(FeatureSelection selection : FeatureSelection.values()){
            for(Balancing bal : Balancing.values()){
                for(Validation val : Validation.values()){
                    Experiment exp = new Experiment(selection,bal,val);
                    for(ClassifierName classifier : ClassifierName.values()){
                        toRet.add(new Classifier(classifier,exp));
                    }
                }
            }
        }
        return toRet;
    }
}
