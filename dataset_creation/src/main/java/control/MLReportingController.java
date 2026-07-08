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

    private static final String[] METRICS = {"Accuracy", "Precision", "Recall", "AUC", "Kappa"};

    @Override
    public void start() throws ControllerException {
        try {
            List<ExperimentResult> data = ReportDAO.loadResults();

            Map<String, List<ExperimentResult>> groups = data.stream()
                    .collect(Collectors.groupingBy(e -> e.cut + "-" + e.selection + "-" + e.balancing + "-" + e.validation));


            Map<String, byte[]> charts = generateAllCharts(groups);

            ReportDAO.saveReport(groups, charts);
            ReportDAO.generateBoxPlotPDF();

            userBoundary.printMessage(new bean.MessageBean("Report salvato correttamente."));
        } catch (PersistenceException | IOException e) {
            throw new ControllerException("Errore durante l'esecuzione del reporting: " + e.getMessage());
        }
    }

    private Map<String, byte[]> generateAllCharts(Map<String, List<ExperimentResult>> groups) throws IOException {
        Map<String, byte[]> charts = new HashMap<>();
        for (Map.Entry<String, List<ExperimentResult>> entry : groups.entrySet()) {
            for (String m : METRICS) {
                charts.put(entry.getKey() + "_" + m, generateChartBytes(entry.getValue(), m));
            }
        }
        return charts;
    }

    private byte[] generateChartBytes(List<ExperimentResult> data, String metric) throws IOException {
        CategoryChart chart = new CategoryChartBuilder().width(600).height(200).title(metric).build();

        List<String> labels = data.stream().map(e -> e.classifier).toList();

        List<Double> values = data.stream().map(e -> {
            if (metric.equals("Accuracy")) return e.acc;
            if (metric.equals("Precision")) return e.prec;
            if (metric.equals("Recall")) return e.rec;
            if (metric.equals("AUC")) return e.auc;
            return e.kap;
        }).toList();

        chart.addSeries(metric, labels, values);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);
        return baos.toByteArray();
    }

    @Override
    public void finish() throws ControllerException {
        // Nothing to do
    }
}