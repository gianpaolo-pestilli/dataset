package dao;

import entity.Classifier;
import exception.PersistenceException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

public class MLDatasetDAO {

    // To write results on the file

    private static String filename = "performance.csv";
    private static String header = "Cut,Selection,Balancing,Validation,Classifier,Accuracy,Precision,Recall,AUC,Kappa,TP,FP,TN,FN";
    private static String dataset = "dataset-weka.arff";



    public static void writeResults(List<Classifier> list) throws PersistenceException{

        // 1. Controlla se il file esiste già
        File file = new File(filename);
        boolean isNewFile = !file.exists();

        // 2. Se è un file nuovo, ci stampa dentro l'header
        if (isNewFile) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
                writer.println(header);
            } catch (IOException e) {
                throw new PersistenceException("Errore durante la scrittura dell'header: " + e.getMessage());
            }
        }

        // 3. Procede con la scrittura di tutti gli esperimenti
        for(Classifier c : list){
            writeClassifier(c);
        }

    }


    private static void writeClassifier(Classifier classf) throws PersistenceException{
        // WARNING: append modality => delete file at every run!!
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
            StringBuilder sb = new StringBuilder();
            entity.Experiment exp = classf.getExperiment();

            sb.append(exp.getCut()).append(",");
            sb.append(exp.getFeatureSelection()).append(",");
            sb.append(exp.getBalancing()).append(",");
            sb.append(exp.getValidation()).append(",");

            // 2. Classifier
            sb.append(classf.getName()).append(",");

            // 3. Accuracy, Precision, Recall, AUC, Kappa (Locale.US per il punto decimale)
            sb.append(String.format(Locale.US, "%.3f", classf.getAccuracy())).append(",");
            sb.append(String.format(Locale.US, "%.3f", classf.getPrecision())).append(",");
            sb.append(String.format(Locale.US, "%.3f", classf.getRecall())).append(",");
            sb.append(String.format(Locale.US, "%.3f", classf.getAUC())).append(",");
            sb.append(String.format(Locale.US, "%.3f", classf.getKappa())).append(",");
            // 5. TP, FP, TN, FN
            sb.append(classf.getTP()).append(",");
            sb.append(classf.getFP()).append(",");
            sb.append(classf.getTN()).append(",");
            sb.append(classf.getFN());

            // Scrive la riga nel file
            writer.println(sb.toString());

        } catch (IOException e) {
           throw new PersistenceException("Errore durante la scrittura dei risultati nel file: " + e.getMessage());
        }
    }

    public static String getDataset(){
        return dataset;
    }

}
