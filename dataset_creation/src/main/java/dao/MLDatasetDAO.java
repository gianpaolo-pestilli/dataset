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

    private static final String apTableFilename = "whatif-ap-table.csv";
    private static final String corrTableFilename = "whatif-correlation-table.csv";

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

    /**
     * Scrive il CSV con la tabella Actual/Expected (What-If Analysis).
     * Sovrascrive il file ad ogni esecuzione.
     * Nota: Dataset B non ha un valore Actual (dataset sintetico), la cella resta vuota.
     */
    public static void writeAPTable(int predictedA, int actualA, int predictedB_plus, int acutalB_plus,
                                    int predictedB,
                                    int predictedC, int actualC) throws PersistenceException {

        try (PrintWriter writer = new PrintWriter(new FileWriter(apTableFilename, false))) {

            writer.println("Dataset,Actual,Expected");

            writer.println("A," + actualA + "," + predictedA);
            writer.println("B+," + acutalB_plus + "," + predictedB_plus);
            writer.println("B,," + predictedB); // Actual vuoto: dataset sintetico
            writer.println("C," + actualC + "," + predictedC);

        } catch (IOException e) {
            throw new PersistenceException("Errore durante la scrittura della AP table: " + e.getMessage());
        }
    }

    /**
     * Scrive il CSV con la tabella Mean/Correlation per ciascuna metrica.
     * Sovrascrive il file ad ogni esecuzione.
     * L'asterisco viene aggiunto al valore di correlazione quando la metrica
     * risulta statisticamente significativa (importantForX = true).
     * Per la riga "numSmells", la correlazione con NSmells stessa è N/A (non applicabile).
     */
    public static void writeCorrTable(List<MetricContainer> containers) throws PersistenceException {

        try (PrintWriter writer = new PrintWriter(new FileWriter(corrTableFilename, false))) {

            writer.println("Variable,MeanA,MeanB_plus,MeanB,MeanC,CorrNSmells,CorrDefectiveness");

            for (MetricContainer c : containers) {

                String corrSmellsStr = c.getMetricName().equals("numSmells")
                        ? "-"
                        : formatCorrelation(c.getCorrWithSmells(), c.isImportantForSmells());

                String corrDefectStr = formatCorrelation(c.getCorrWithDefects(), c.isImportantForDefects());

                StringBuilder sb = new StringBuilder();
                sb.append(c.getMetricName()).append(",");
                sb.append(String.format(Locale.US, "%.2f", c.getMeanA())).append(",");
                sb.append(String.format(Locale.US, "%.2f", c.getMeanB_plus())).append(",");
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