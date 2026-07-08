package entity;

import settings.DataType;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WekaException;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.Arrays;
import java.util.List;

public class TestChampion {

    private String datasetA = "dataset-weka.arff";
    private String datasetB_plus = "dataset_B_plus.arff";
    private String datasetB = "dataset_B.arff";
    private String datasetC = "dataset_C.arff";

    FilteredClassifier trainedModel;

    public TestChampion(FilteredClassifier fc){
        this.trainedModel = fc;
    }
    // Stessa lista esclusa dal log-transform usata in TrainChampion/WekaWorker
    private static final List<String> EXCLUDED_ATTRS = Arrays.asList(
            "ReleaseID", "numAuthorsFromBegin", "avgChangeSetFromBegin",
            "maxChangeSetFromBegin", "age"
    );

    /**
     * Applica il modello già allenato al dataset indicato e conta quante istanze
     * vengono PREDETTE come buggy (colonna "Expected" della tabella What-If).
     */
    public int predictBuggyCount(DataType type) throws WekaException {
        String datasetFile;
        switch (type){
            case B -> datasetFile = datasetB;
            case B_PLUS -> datasetFile = datasetB_plus;
            case C -> datasetFile = datasetC;
            case null,default -> datasetFile = datasetA;
        }

        Instances dataset = loadDataset(datasetFile);
        applyLogTransform(dataset);

        int buggyClassIndex = dataset.classAttribute().indexOfValue("true");
        if (buggyClassIndex == -1) buggyClassIndex = 1;

        int count = 0;
        try {
            for (Instance inst : dataset) {
                double pred = trainedModel.classifyInstance(inst);
                if (pred == buggyClassIndex) count++;
            }
        } catch (Exception e) {
            throw new WekaException("Errore predizione: " + e.getMessage());
        }

        return count;
    }

    /**
     * Conta quante istanze sono REALMENTE buggy nel dataset (colonna "Actual").
     * NON usare su Dataset B: essendo sintetico (NSmells forzato a 0), non ha
     * un valore Actual osservabile nella realtà.
     */
    public int actualBuggyCount(DataType type) throws WekaException {

        String datasetFile;
        switch (type){
            case B -> datasetFile = datasetB;
            case B_PLUS -> datasetFile = datasetB_plus;
            case C -> datasetFile = datasetC;
            case null, default -> datasetFile = datasetA;
        }

        Instances dataset = loadDataset(datasetFile);

        int buggyClassIndex = dataset.classAttribute().indexOfValue("true");
        if (buggyClassIndex == -1) buggyClassIndex = 1;

        int count = 0;
        for (Instance inst : dataset) {
            if (inst.classValue() == buggyClassIndex) count++;
        }

        return count;
    }

    private Instances loadDataset(String datasetFile) throws WekaException {
        Instances dataset;
        try {
            DataSource source = new DataSource(datasetFile);
            dataset = source.getDataSet();
        } catch (Exception e) {
            throw new WekaException("Errore caricamento: " + e.getMessage());
        }
        if (dataset.classIndex() == -1) dataset.setClassIndex(dataset.numAttributes() - 1);
        return dataset;
    }

    private void applyLogTransform(Instances dataset) {
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
    }
}