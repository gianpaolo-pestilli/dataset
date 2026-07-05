package dao;

import entity.Classifier;

import java.util.List;

public class MLDatasetDAO {

    // To write results on the file

    private static String filename = "performance.csv";
    private static String header = "Balancing,Selection,Validation,Classifier,Precision,Recall,AUC,Kappa,NPofB20,Accuracy";
    private static String dataset = "dataset-weka.arff";

    public static void writeResults(List<Classifier> list){

    }

}
