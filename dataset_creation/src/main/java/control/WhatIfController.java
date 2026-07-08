package control;

import dao.MLDatasetDAO;
import dao.TableDAO;
import entity.*;
import exception.ControllerException;
import exception.PersistenceException;
import settings.*;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.WekaException;
import java.util.List;

public class WhatIfController extends AppController{

    private String datasetA = "dataset-weka.arff";
    private String datasetB_plus = "dataset_B_plus.arff";
    private String datasetB = "dataset_B.arff";
    private String datasetC = "dataset_C.arff";

    private FilteredClassifier trainedModel;

    private int predictedA;
    private int actualA;

    private int predictedB_plus;
    private int actualB_plus;

    private int predictedB;

    private int predictedC;
    private int actualC;

    private List<MetricContainer> containers; // One for each metric

    @Override
    public void start() throws ControllerException {
        trainModel();
        testModel();
        fillTheTable();

    }

    @Override
    public void finish() throws ControllerException {

        try{
        MLDatasetDAO.writeAPTable(predictedA,actualA,predictedB_plus, actualB_plus,predictedB,predictedC,actualC);
        MLDatasetDAO.writeCorrTable(containers);
        TableDAO.writeGraphicAPTable(predictedA,actualA,predictedB_plus, actualB_plus,predictedB,predictedC,actualC);
        TableDAO.writeGraphicCorrTable(containers);
        } catch (PersistenceException e) {
            throw new ControllerException(e.getMessage());
        }
    }

    private void trainModel()throws ControllerException{
        try {
            Experiment exp = new Experiment(FeatureSelection.FILTER, Balancing.SMOTE, null, ManualCut.YES);
            Classifier cls = new Classifier(ClassifierName.RANDOM_FOREST, exp);
            TrainChampion trainer = new TrainChampion(cls);
            FilteredClassifier fc = trainer.train();
            this.trainedModel = fc;
        } catch (WekaException w){
            throw new ControllerException(w.getMessage());
        }

    }



    private void testModel() throws ControllerException{

        try {
            TestChampion tester = new TestChampion(trainedModel);
            predictedA = tester.predictBuggyCount(null);
            actualA = tester.actualBuggyCount(null);

            predictedB_plus = tester.predictBuggyCount(DataType.B_PLUS);
            actualB_plus = tester.actualBuggyCount(DataType.B_PLUS);

            predictedB = tester.predictBuggyCount(DataType.B);

            predictedC = tester.predictBuggyCount(DataType.C);
            actualC = tester.actualBuggyCount(DataType.C);

        } catch (WekaException w){
            throw new ControllerException(w.getMessage());
        }
    }

    private void fillTheTable() throws ControllerException {
        MLDataController mlData = new MLDataController();
        mlData.start(); // popola A, B+, B, C in memoria

        CorrelationAnalyzer analyzer = new CorrelationAnalyzer();
        this.containers = analyzer.computeTable(
                mlData.getAllDataset(),
                mlData.getSmelledDataset(),
                mlData.getCleanedDataset(),
                mlData.getNotSmelledDataset()
        );
    }
}
