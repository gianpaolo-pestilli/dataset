package control;

import dao.ReportDAO;
import entity.ExperimentResult;
import exception.ControllerException;
import exception.PersistenceException;
import org.knowm.xchart.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MLReportingController extends AppController {

    @Override
    public void start() throws ControllerException {
        try {

            List<ExperimentResult> data = ReportDAO.loadResults();

            Map<String, List<ExperimentResult>> groups = data.stream()
                    .collect(Collectors.groupingBy(e -> e.cut + "-" + e.selection + "-" + e.balancing + "-" + e.validation));

            // Genera grafici in memoria (byte[]) per passarli al DAO
            Map<String, byte[]> charts = new HashMap<>();
            try {
                for (String key : groups.keySet()) {
                    String[] metrics = {"Accuracy", "Precision", "Recall", "AUC", "Kappa"};
                    for (String m : metrics) {
                        charts.put(key + "_" + m, generateChartBytes(groups.get(key), m));
                    }
                }
            } catch (IOException e) {
                throw new ControllerException(e.getMessage());
            }

            // Persistenza delegata al DAO
            ReportDAO.saveReport(groups, charts);
            ReportDAO.generateBoxPlotPDF();

            userBoundary.printMessage(new bean.MessageBean("Report salvato in nel file "));
        } catch (PersistenceException e) {
            throw new ControllerException("Errore di persistenza: " + e.getMessage());
        }
    }

    private byte[] generateChartBytes(List<ExperimentResult> data, String metric) throws IOException {
        CategoryChart chart = new CategoryChartBuilder().width(600).height(200).title(metric).build();
        chart.addSeries(metric,
                data.stream().map(e -> e.classifier).collect(Collectors.toList()),
                data.stream().map(e -> {
                    if (metric.equals("Accuracy")) return e.acc;
                    if (metric.equals("Precision")) return e.prec;
                    if (metric.equals("Recall")) return e.rec;
                    if (metric.equals("AUC")) return e.auc;
                    return e.kap;
                }).collect(Collectors.toList()));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);
        return baos.toByteArray();
    }

    @Override
    public void finish() throws ControllerException {
        // Nessun file temporaneo da pulire, abbiamo usato ByteArrayOutputStream!
    }
}