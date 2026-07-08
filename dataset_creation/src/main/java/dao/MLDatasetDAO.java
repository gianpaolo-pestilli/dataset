package dao;

import entity.Classifier;
import entity.MetricContainer;
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

    private static final String AP_TABLE_FILENAME = "whatif-ap-table.csv";
    private static final String CORR_TABLE_FILENAME = "whatif-correlation-table.csv";

    private MLDatasetDAO(){
        // Make it private
    }

    public static void writeResults(List<Classifier> list) throws PersistenceException{


        File file = new File(filename);
        boolean isNewFile = !file.exists();


        if (isNewFile) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
                writer.println(header);
            } catch (IOException e) {
                throw new PersistenceException("Errore durante la scrittura dell'header: " + e.getMessage());
            }
        }


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


            sb.append(classf.getName()).append(",");


            sb.append(String.format(Locale.US, "%.3f", classf.getAccuracy())).append(",");
            sb.append(String.format(Locale.US, "%.3f", classf.getPrecision())).append(",");
            sb.append(String.format(Locale.US, "%.3f", classf.getRecall())).append(",");
            sb.append(String.format(Locale.US, "%.3f", classf.getAuc())).append(",");
            sb.append(String.format(Locale.US, "%.3f", classf.getKappa())).append(",");

            sb.append(classf.getTp()).append(",");
            sb.append(classf.getFp()).append(",");
            sb.append(classf.getTn()).append(",");
            sb.append(classf.getFn());


            writer.println(sb.toString());

        } catch (IOException e) {
            throw new PersistenceException("Errore durante la scrittura dei risultati nel file: " + e.getMessage());
        }
    }

    public static String getDataset(){
        return dataset;
    }


    public static void writeAPTable(int predictedA, int actualA, int predictedBplus, int acutalBplus,
                                    int predictedB,
                                    int predictedC, int actualC) throws PersistenceException {

        try (PrintWriter writer = new PrintWriter(new FileWriter(AP_TABLE_FILENAME, false))) {

            writer.println("Dataset,Actual,Expected");

            writer.println("A," + actualA + "," + predictedA);
            writer.println("B+," + acutalBplus + "," + predictedBplus);
            writer.println("B,," + predictedB); // Actual vuoto: dataset sintetico
            writer.println("C," + actualC + "," + predictedC);

        } catch (IOException e) {
            throw new PersistenceException("Errore durante la scrittura della AP table: " + e.getMessage());
        }
    }


    public static void writeCorrTable(List<MetricContainer> containers) throws PersistenceException {

        try (PrintWriter writer = new PrintWriter(new FileWriter(CORR_TABLE_FILENAME, false))) {

            writer.println("Variable,MeanA,MeanB_plus,MeanB,MeanC,CorrNSmells,CorrDefectiveness");

            for (MetricContainer c : containers) {

                String corrSmellsStr = c.getMetricName().equals("numSmells")
                        ? "-"
                        : formatCorrelation(c.getCorrWithSmells(), c.isImportantForSmells());

                String corrDefectStr = formatCorrelation(c.getCorrWithDefects(), c.isImportantForDefects());

                StringBuilder sb = new StringBuilder();
                sb.append(c.getMetricName()).append(",");
                sb.append(String.format(Locale.US, "%.2f", c.getMeanA())).append(",");
                sb.append(String.format(Locale.US, "%.2f", c.getMeanBplus())).append(",");
                sb.append(String.format(Locale.US, "%.2f", c.getMeanB())).append(",");
                sb.append(String.format(Locale.US, "%.2f", c.getMeanC())).append(",");
                sb.append(corrSmellsStr).append(",");
                sb.append(corrDefectStr);

                writer.println(sb.toString());
            }

        } catch (IOException e) {
            throw new PersistenceException("Errore durante la scrittura della Correlation table: " + e.getMessage());
        }
    }

    private static String formatCorrelation(double r, boolean significant) {
        String base = String.format(Locale.US, "%.2f", r);
        return significant ? base + "*" : base;
    }

}