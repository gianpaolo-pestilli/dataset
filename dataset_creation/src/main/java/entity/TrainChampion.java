package entity;

import settings.Balancing;


import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.WekaException;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.Remove;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrainChampion {

    private String datasetFile = "dataset-weka.arff";
    private Classifier classifier;


    public TrainChampion(Classifier c){
        this.classifier = c;
    }

    // Stessa lista esclusa dal log-transform usata in WekaWorker
    private static final List<String> EXCLUDED_ATTRS = Arrays.asList(
            "ReleaseID", "numAuthorsFromBegin", "avgChangeSetFromBegin",
            "maxChangeSetFromBegin", "age"
    );

    private FilteredClassifier trainedModel;

    public FilteredClassifier train() throws WekaException {

        Experiment exp = classifier.getExperiment();

        // 1. CARICAMENTO
        Instances dataset;
        try {
            DataSource source = new DataSource(datasetFile);
            dataset = source.getDataSet();
        } catch (Exception e) {
            throw new WekaException("Errore caricamento: " + e.getMessage());
        }

        if (dataset.classIndex() == -1) dataset.setClassIndex(dataset.numAttributes() - 1);

        // 2. TRASFORMAZIONE MANUALE (LOG) - identica a WekaWorker
        for (int i = 0; i < dataset.numAttributes(); i++) {
            if (dataset.attribute(i).isNumeric() &&
                    i != dataset.classIndex() &&
                    !EXCLUDED_ATTRS.contains(dataset.attribute(i).name())) {

                for (int j = 0; j < dataset.numInstances(); j++) {
                    double val = dataset.instance(j).value(i);
                    dataset.instance(j).setValue(i, Math.log1p(val));
                }
            }
        }

        // 3. SETUP CLASSIFICATORE E PIPELINE (stessa logica di WekaWorker)
        weka.classifiers.Classifier baseModel = switch (classifier.getName()) {
            case RANDOM_FOREST -> new RandomForest();
            case NAIVE_BAYES -> new NaiveBayes();
            case IBK -> new IBk();
        };

        List<Filter> filterList = new ArrayList<>();

        if (exp.getCut() == settings.ManualCut.YES) {
            Remove removeFilter = new Remove();
            removeFilter.setAttributeIndices("1-3");
            filterList.add(removeFilter);
        }

        if (exp.getFeatureSelection() != settings.FeatureSelection.NO) {
            AttributeSelection fs = new AttributeSelection();
            fs.setEvaluator(new CfsSubsetEval());
            GreedyStepwise search = new GreedyStepwise();
            search.setSearchBackwards(true);
            fs.setSearch(search);
            filterList.add(fs);
        }

        if (exp.getBalancing() == Balancing.SMOTE) {
            filterList.add(new SMOTE());
        } else if (exp.getBalancing() == Balancing.UNDERSAMPLING) {
            SpreadSubsample s = new SpreadSubsample();
            s.setDistributionSpread(1.0);
            filterList.add(s);
        }

        FilteredClassifier fc = new FilteredClassifier();
        if (!filterList.isEmpty()) {
            MultiFilter mf = new MultiFilter();
            mf.setFilters(filterList.toArray(new Filter[0]));
            fc.setFilter(mf);
        }
        fc.setClassifier(baseModel);

        // 4. TRAINING UNICO SU TUTTO IL DATASET (niente CV, niente split)
        try {
            fc.buildClassifier(dataset);
        } catch (Exception e) {
            throw new WekaException("Errore training: " + e.getMessage());
        }

        this.trainedModel = fc;
        return fc;
    }

    public FilteredClassifier getTrainedModel() {
        return trainedModel;
    }
}