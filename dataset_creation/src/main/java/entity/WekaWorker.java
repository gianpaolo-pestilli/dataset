package entity;

import settings.Balancing;
import settings.ClassifierName;
import settings.Validation;

import weka.classifiers.Evaluation;
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
import java.util.Random;

public class WekaWorker {

    private final Classifier classifier;
    private final String datasetFile;

    public WekaWorker(Classifier cls, String filepath) {
        this.classifier = cls;
        this.datasetFile = filepath;
    }

    public void run() throws WekaException {
        Instances dataset = loadDataset();
        int buggyClassIndex = getBuggyClassIndex(dataset);

        applyLogTransformation(dataset, buggyClassIndex);

        Experiment exp = classifier.getExperiment();
        FilteredClassifier fc = createPipeline(exp);

        Evaluation eval = performValidation(fc, dataset, exp);

        updateMetrics(eval, buggyClassIndex);
    }

    private Instances loadDataset() throws WekaException {
        try {
            DataSource source = new DataSource(datasetFile);
            Instances dataset = source.getDataSet();
            if (dataset.classIndex() == -1) dataset.setClassIndex(dataset.numAttributes() - 1);
            return dataset;
        } catch (Exception e) {
            throw new WekaException("Errore caricamento: " + e.getMessage());
        }
    }

    private int getBuggyClassIndex(Instances dataset) {
        int index = dataset.classAttribute().indexOfValue("true");
        return (index == -1) ? 1 : index;
    }

    private void applyLogTransformation(Instances dataset, int buggyClassIndex) {
        List<String> excludedAttrs = Arrays.asList(
                "ReleaseID", "numAuthorsFromBegin", "avgChangeSetFromBegin",
                "maxChangeSetFromBegin", "age"
        );

        for (int i = 0; i < dataset.numAttributes(); i++) {
            if (dataset.attribute(i).isNumeric() && i != dataset.classIndex() && !excludedAttrs.contains(dataset.attribute(i).name())) {
                for (int j = 0; j < dataset.numInstances(); j++) {
                    double val = dataset.instance(j).value(i);
                    dataset.instance(j).setValue(i, Math.log1p(val));
                }
            }
        }
    }

    private FilteredClassifier createPipeline(Experiment exp) {
        weka.classifiers.Classifier baseModel = switch (classifier.getName()) {
            case RANDOM_FOREST -> new RandomForest();
            case NAIVE_BAYES -> new NaiveBayes();
            case IBK -> new IBk();
            default -> new RandomForest();
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

        if (exp.getBalancing() == settings.Balancing.SMOTE) {
            filterList.add(new SMOTE());
        } else if (exp.getBalancing() == settings.Balancing.UNDERSAMPLING) {
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
        return fc;
    }

    private Evaluation performValidation(FilteredClassifier fc, Instances dataset, Experiment exp) throws WekaException {
        try {
            Evaluation eval;
            if (exp.getValidation() == settings.Validation.TEN_T_TEN_F) {
                eval = new Evaluation(dataset);
                for (int i = 0; i < 10; i++) {
                    eval.crossValidateModel(fc, dataset, 10, new Random(i));
                }
            } else {
                int trainSize = (int) Math.round(dataset.numInstances() * 0.80);
                Instances train = new Instances(dataset, 0, trainSize);
                Instances test = new Instances(dataset, trainSize, dataset.numInstances() - trainSize);
                fc.buildClassifier(train);
                eval = new Evaluation(train);
                eval.evaluateModel(fc, test);
            }
            return eval;
        } catch (Exception e) {
            throw new WekaException(e.getMessage());
        }
    }

    private void updateMetrics(Evaluation eval, int buggyClassIndex) {
        classifier.setAccuracy(eval.pctCorrect() / 100.0);
        classifier.setKappa(eval.kappa());
        classifier.setPrecision(eval.precision(buggyClassIndex));
        classifier.setRecall(eval.recall(buggyClassIndex));
        classifier.setAUC(eval.areaUnderROC(buggyClassIndex));
        classifier.setTP((long) eval.numTruePositives(buggyClassIndex));
        classifier.setFP((long) eval.numFalsePositives(buggyClassIndex));
        classifier.setTN((long) eval.numTrueNegatives(buggyClassIndex));
        classifier.setFN((long) eval.numFalseNegatives(buggyClassIndex));
    }
}