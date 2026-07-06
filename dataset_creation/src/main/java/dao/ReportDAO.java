package dao;

import entity.ExperimentResult;
import exception.PersistenceException;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReportDAO {

    private static final String PATH_CSV = "performance.csv";
    private static final String PATH_PDF = "Grafici_classificatori.pdf";

    public static List<ExperimentResult> loadResults() throws PersistenceException {
        try {
            // Aggiunto filtro per righe vuote e gestione sicura
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
        // Try-with-resources per chiudere automaticamente il documento
        try (PDDocument doc = new PDDocument()) {
            for (Map.Entry<String, List<ExperimentResult>> entry : groupedData.entrySet()) {

                // Pagina 1: Grafici
                PDPage graphPage = new PDPage();
                doc.addPage(graphPage);

                // Try-with-resources per chiudere lo stream della pagina dei grafici
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

                // Pagina 2: Matrice di confusione (sempre su pagina separata per pulizia)
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

        // Header
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

        // Righe
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
}