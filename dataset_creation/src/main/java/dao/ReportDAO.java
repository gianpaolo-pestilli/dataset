package dao;

import entity.ExperimentResult;
import exception.PersistenceException;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ReportDAO {

    private static final String PATH_CSV = "performance.csv";
    private static final String PATH_PDF = "Grafici_classificatori.pdf";

    public static List<ExperimentResult> loadResults() throws PersistenceException {
        try {
            return Files.readAllLines(Paths.get(PATH_CSV)).stream()
                    .skip(1)
                    .filter(l -> !l.isBlank())
                    .map(l -> new ExperimentResult(l.split(",")))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new PersistenceException("Errore durante la lettura del file CSV: " + e.getMessage());
        }
    }

    public static void saveReport(Map<String, List<ExperimentResult>> groupedData, Map<String, byte[]> charts) throws PersistenceException {
        try (PDDocument doc = new PDDocument()) {
            for (Map.Entry<String, List<ExperimentResult>> entry : groupedData.entrySet()) {

                PDPage graphPage = new PDPage();
                doc.addPage(graphPage);

                try (PDPageContentStream content = new PDPageContentStream(doc, graphPage)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                    content.newLineAtOffset(50, 750);
                    content.showText("Esperimento: " + entry.getKey());
                    content.endText();

                    int yPos = 700;
                    String[] metrics = {"Accuracy", "Precision", "Recall", "AUC", "Kappa"};
                    for (String m : metrics) {
                        byte[] imgData = charts.get(entry.getKey() + "_" + m);
                        if (imgData != null) {
                            PDImageXObject img = PDImageXObject.createFromByteArray(doc, imgData, m);
                            content.drawImage(img, 50, yPos - 120, 400, 120);
                            yPos -= 130;
                        }
                    }
                }

                PDPage tablePage = new PDPage();
                doc.addPage(tablePage);
                try (PDPageContentStream tableContent = new PDPageContentStream(doc, tablePage)) {
                    drawConfusionMatrixTable(tableContent, entry.getValue(), 750);
                }
            }
            doc.save(PATH_PDF);
        } catch (IOException e) {
            throw new PersistenceException("Errore critico durante la generazione PDF: " + e.getMessage());
        }
    }

    private static void drawConfusionMatrixTable(PDPageContentStream content, List<ExperimentResult> data, int startY) throws IOException {
        float margin = 50;
        float y = startY;
        float rowHeight = 20;
        float[] colWidths = {120f, 40f, 40f, 40f, 40f};
        String[] headers = {"Classifier", "TP", "FP", "TN", "FN"};

        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);

        float x = margin;
        for (int i = 0; i < headers.length; i++) {
            content.addRect(x, y, colWidths[i], rowHeight);
            content.stroke();
            content.beginText();
            content.newLineAtOffset(x + 5, y + 6);
            content.showText(headers[i]);
            content.endText();
            x += colWidths[i];
        }

        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
        for (ExperimentResult e : data) {
            y -= rowHeight;
            x = margin;
            String[] rowData = {e.classifier, String.valueOf(e.tp), String.valueOf(e.fp), String.valueOf(e.tn), String.valueOf(e.fn)};

            for (int i = 0; i < rowData.length; i++) {
                content.addRect(x, y, colWidths[i], rowHeight);
                content.stroke();
                content.beginText();
                content.newLineAtOffset(x + 5, y + 6);
                content.showText(rowData[i]);
                content.endText();
                x += colWidths[i];
            }
        }
    }

    // =============================================================
    //  GENERA UN BOX PLOT PER OGNI METRICA IN UN PDF SEPARATO
    //  (UNA PAGINA PER METRICA, 3 CLASSIFICATORI PER GRAFICO)
    // =============================================================
    public static void generateBoxPlotPDF() throws PersistenceException {
        List<ExperimentResult> results = loadResults();

        Map<String, List<ExperimentResult>> groups = results.stream()
                .collect(Collectors.groupingBy(r -> r.classifier));

        String[] metricNames = {"Accuracy", "Precision", "Recall", "AUC", "Kappa"};
        Map<String, Map<String, List<Double>>> metricData = new LinkedHashMap<>();

        for (String metric : metricNames) {
            Map<String, List<Double>> classifierValues = new LinkedHashMap<>();
            for (Map.Entry<String, List<ExperimentResult>> entry : groups.entrySet()) {
                String cl = entry.getKey();
                List<Double> values = new ArrayList<>();
                for (ExperimentResult r : entry.getValue()) {
                    switch (metric) {
                        case "Accuracy": values.add(r.acc); break;
                        case "Precision": values.add(r.prec); break;
                        case "Recall": values.add(r.rec); break;
                        case "AUC": values.add(r.auc); break;
                        case "Kappa": values.add(r.kap); break;
                    }
                }
                classifierValues.put(cl, values);
            }
            metricData.put(metric, classifierValues);
        }

        try (PDDocument doc = new PDDocument()) {
            for (String metric : metricNames) {
                byte[] imgBytes = createSingleMetricBoxPlotImage(metric, metricData.get(metric));
                PDPage page = new PDPage();
                doc.addPage(page);

                PDImageXObject img = PDImageXObject.createFromByteArray(doc, imgBytes, metric);
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();
                float imgWidth = img.getWidth();
                float imgHeight = img.getHeight();
                float scaleX = (pageWidth - 80) / imgWidth;
                float scaleY = (pageHeight - 80) / imgHeight;
                float scale = Math.min(scaleX, scaleY);
                float scaledWidth = imgWidth * scale;
                float scaledHeight = imgHeight * scale;
                float x = (pageWidth - scaledWidth) / 2;
                float y = (pageHeight - scaledHeight) / 2;

                try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                    content.drawImage(img, x, y, scaledWidth, scaledHeight);
                }
            }
            doc.save("BoxPlot.pdf");
        } catch (IOException e) {
            throw new PersistenceException("Errore durante la generazione del box plot PDF: " + e.getMessage());
        }
    }

    // =============================================================
    //  GENERA L'IMMAGINE DEL BOX PLOT PER UNA SINGOLA METRICA
    // =============================================================
    private static byte[] createSingleMetricBoxPlotImage(String metricName, Map<String, List<Double>> classifierValues) throws IOException {
        int width = 950;
        int height = 650;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // ----- MARGINI (adattati per la legenda in alto a destra) -----
        int marginTop = 80;
        int marginBottom = 100;
        int marginLeft = 90;
        int marginRight = 180; // aumentato per fare spazio alla legenda
        int plotWidth = width - marginLeft - marginRight;
        int plotHeight = height - marginTop - marginBottom;

        double yMin = 0.0;
        double yMax = 1.0;
        int yBase = marginTop + plotHeight;

        // ----- TITOLO -----
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        String title = "Box Plot - " + metricName;
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - titleWidth) / 2, marginTop - 20);

        // ----- ASSI -----
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(marginLeft, marginTop, marginLeft, marginTop + plotHeight);
        g.drawLine(marginLeft, marginTop + plotHeight, marginLeft + plotWidth, marginTop + plotHeight);

        // ----- TICK Y -----
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (double v = 0.0; v <= 1.0; v += 0.1) {
            int yTick = (int) (yBase - (v / yMax) * plotHeight);
            g.drawLine(marginLeft - 6, yTick, marginLeft, yTick);
            g.drawString(String.format("%.1f", v), marginLeft - 40, yTick + 4);
        }

        // ----- ETICHETTA ASSE Y -----
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("Metric Value", marginLeft - 70, marginTop - 10);

        // ----- CLASSIFICATORI E COLORI -----
        String[] classifiers = {"RANDOM_FOREST", "NAIVE_BAYES", "IBK"};
        Color[] colors = {
                new Color(31, 119, 180),
                new Color(255, 127, 14),
                new Color(44, 160, 44)
        };

        // ----- POSIZIONAMENTO BOX -----
        int totalBoxes = classifiers.length;
        int boxWidth = Math.min(65, (plotWidth - (totalBoxes - 1) * 40) / totalBoxes);
        int spacing = Math.min(40, (plotWidth - totalBoxes * boxWidth) / (totalBoxes - 1));
        if (spacing < 10) spacing = 10;

        int currentX = marginLeft + (plotWidth - totalBoxes * boxWidth - (totalBoxes - 1) * spacing) / 2;

        // ----- DISEGNA I BOX -----
        for (int cIdx = 0; cIdx < classifiers.length; cIdx++) {
            String cl = classifiers[cIdx];
            List<Double> values = classifierValues.get(cl);
            if (values == null || values.isEmpty()) continue;

            Collections.sort(values);
            double min = values.get(0);
            double max = values.get(values.size() - 1);
            double q1 = percentile(values, 0.25);
            double median = percentile(values, 0.50);
            double q3 = percentile(values, 0.75);

            int yMinPix = (int) (yBase - (min / yMax) * plotHeight);
            int yQ1Pix = (int) (yBase - (q1 / yMax) * plotHeight);
            int yMedPix = (int) (yBase - (median / yMax) * plotHeight);
            int yQ3Pix = (int) (yBase - (q3 / yMax) * plotHeight);
            int yMaxPix = (int) (yBase - (max / yMax) * plotHeight);

            // ----- ETICHETTA SOTTO L'ASSE X -----
            String label = cl.replace("_", " ");
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            int labelWidth = g.getFontMetrics().stringWidth(label);
            g.drawString(label, currentX + boxWidth / 2 - labelWidth / 2, marginTop + plotHeight + 55);

            // ----- WHISKER -----
            g.setColor(colors[cIdx]);
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(currentX + boxWidth / 2, yMinPix, currentX + boxWidth / 2, yMaxPix);

            // ----- BOX -----
            int boxTop = Math.min(yQ1Pix, yQ3Pix);
            int boxHeight = Math.abs(yQ1Pix - yQ3Pix);
            if (boxHeight < 1) boxHeight = 1;
            g.fillRect(currentX, boxTop, boxWidth, boxHeight);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1.2f));
            g.drawRect(currentX, boxTop, boxWidth, boxHeight);

            // ----- MEDIANA -----
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2.5f));
            g.drawLine(currentX + 2, yMedPix, currentX + boxWidth - 2, yMedPix);

            // ----- MIN E MAX (TICK) -----
            g.setStroke(new BasicStroke(1.5f));
            int tickLen = boxWidth / 4;
            g.drawLine(currentX + boxWidth / 2 - tickLen, yMinPix, currentX + boxWidth / 2 + tickLen, yMinPix);
            g.drawLine(currentX + boxWidth / 2 - tickLen, yMaxPix, currentX + boxWidth / 2 + tickLen, yMaxPix);

            currentX += boxWidth + spacing;
        }

        // ----- LEGENDA (in alto a destra, fuori dall'area del grafico) -----
        int legX = marginLeft + plotWidth + 20;
        int legY = marginTop + 20;
        int legWidth = 150;
        int legHeight = 110;
        g.setColor(new Color(240, 240, 240));
        g.fillRoundRect(legX, legY, legWidth, legHeight, 8, 8);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(legX, legY, legWidth, legHeight, 8, 8);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("Classifiers", legX + 12, legY + 18);

        for (int i = 0; i < classifiers.length; i++) {
            g.setColor(colors[i]);
            g.fillRect(legX + 12, legY + 28 + i * 24, 15, 15);
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String shortName = classifiers[i].replace("_", " ");
            g.drawString(shortName, legX + 35, legY + 39 + i * 24);
        }

        g.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        }
    }

    // =============================================================
    //  CALCOLA IL PERCENTILE SU UNA LISTA ORDINATA
    // =============================================================
    private static double percentile(List<Double> sortedValues, double percentile) {
        int n = sortedValues.size();
        double index = (n - 1) * percentile;
        int low = (int) Math.floor(index);
        int high = (int) Math.ceil(index);
        if (low == high) return sortedValues.get(low);
        double fraction = index - low;
        return sortedValues.get(low) * (1 - fraction) + sortedValues.get(high) * fraction;
    }
}