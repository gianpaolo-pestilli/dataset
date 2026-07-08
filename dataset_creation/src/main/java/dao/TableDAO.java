package dao;

import entity.MetricContainer;
import exception.PersistenceException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class TableDAO {

    private static final String AP_TABLE_GRAPHIC_FILENAME = "whatif-ap-table.png";
    private static final String CORR_TABLE_GRAPHIC_FILENAME = "whatif-correlation-table.png";

    private static final int ROW_HEIGHT = 32;
    private static final int PADDING = 12;
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font CELL_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private TableDAO(){
        // Making it private
    }

    public static void writeGraphicAPTable(int predictedA, int actualA,
                                           int predictedBplus,
                                           int acutalBplus, int predictedB,
                                           int predictedC, int actualC) throws PersistenceException {

        String[] headers = {"Dataset", "Actual", "Expected"};

        String[][] rows = {
                {"A", String.valueOf(actualA), String.valueOf(predictedA)},
                {"B+", String.valueOf(acutalBplus), String.valueOf(predictedBplus)},
                {"B", "-", String.valueOf(predictedB)}, // Actual non disponibile: dataset sintetico
                {"C", String.valueOf(actualC), String.valueOf(predictedC)}
        };

        drawTable(headers, rows, AP_TABLE_GRAPHIC_FILENAME);
    }

    public static void writeGraphicCorrTable(List<MetricContainer> containers) throws PersistenceException {

        String[] headers = {"Variable", "Mean A", "Mean B+", "Mean B", "Mean C", "Corr. NSmells", "Corr. Defectiveness"};

        String[][] rows = new String[containers.size()][7];

        for (int i = 0; i < containers.size(); i++) {
            MetricContainer c = containers.get(i);

            String corrSmellsStr = c.getMetricName().equals("numSmells")
                    ? "-"
                    : formatCorrelation(c.getCorrWithSmells(), c.isImportantForSmells());

            String corrDefectStr = formatCorrelation(c.getCorrWithDefects(), c.isImportantForDefects());

            rows[i][0] = c.getMetricName();
            rows[i][1] = String.format(Locale.US, "%.2f", c.getMeanA());
            rows[i][2] = String.format(Locale.US, "%.2f", c.getMeanBplus());
            rows[i][3] = String.format(Locale.US, "%.2f", c.getMeanB());
            rows[i][4] = String.format(Locale.US, "%.2f", c.getMeanC());
            rows[i][5] = corrSmellsStr;
            rows[i][6] = corrDefectStr;
        }

        drawTable(headers, rows, CORR_TABLE_GRAPHIC_FILENAME);
    }

    private static String formatCorrelation(double r, boolean significant) {
        String base = String.format(Locale.US, "%.2f", r);
        return significant ? base + "*" : base;
    }

    /**
     * Disegna una tabella (header + righe) su un'immagine PNG, con bordi e
     * larghezza colonne adattata al contenuto più lungo.
     */
    private static void drawTable(String[] headers, String[][] rows, String outputFilename) throws PersistenceException {

        int numCols = headers.length;
        int numRows = rows.length + 1; // +1 per l'header

        // Calcolo temporaneo per misurare la larghezza del testo più lungo per colonna
        BufferedImage temp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D measurer = temp.createGraphics();
        measurer.setFont(HEADER_FONT);
        FontMetrics headerMetrics = measurer.getFontMetrics();
        measurer.setFont(CELL_FONT);
        FontMetrics cellMetrics = measurer.getFontMetrics();

        int[] colWidths = new int[numCols];
        for (int col = 0; col < numCols; col++) {
            int maxWidth = headerMetrics.stringWidth(headers[col]);
            for (String[] row : rows) {
                maxWidth = Math.max(maxWidth, cellMetrics.stringWidth(row[col]));
            }
            colWidths[col] = maxWidth + 2 * PADDING;
        }
        measurer.dispose();

        int totalWidth = 0;
        for (int w : colWidths) totalWidth += w;
        int totalHeight = numRows * ROW_HEIGHT;

        BufferedImage image = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Sfondo bianco
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, totalWidth, totalHeight);

        // Header
        int x = 0;
        g.setColor(new Color(230, 230, 230));
        g.fillRect(0, 0, totalWidth, ROW_HEIGHT);
        g.setFont(HEADER_FONT);
        g.setColor(Color.BLACK);
        for (int col = 0; col < numCols; col++) {
            g.drawString(headers[col], x + PADDING, ROW_HEIGHT - PADDING + 2);
            x += colWidths[col];
        }

        // Righe
        g.setFont(CELL_FONT);
        for (int row = 0; row < rows.length; row++) {
            int y = (row + 1) * ROW_HEIGHT;
            x = 0;
            for (int col = 0; col < numCols; col++) {
                g.drawString(rows[row][col], x + PADDING, y + ROW_HEIGHT - PADDING + 2);
                x += colWidths[col];
            }
        }

        // Bordi (griglia)
        g.setColor(Color.GRAY);
        int y = 0;
        for (int row = 0; row <= numRows; row++) {
            g.drawLine(0, y, totalWidth, y);
            y += ROW_HEIGHT;
        }
        x = 0;
        for (int col = 0; col <= numCols; col++) {
            g.drawLine(x, 0, x, totalHeight);
            if (col < numCols) x += colWidths[col];
        }

        g.dispose();

        try {
            ImageIO.write(image, "png", new File(outputFilename));
        } catch (IOException e) {
            throw new PersistenceException("Errore durante la scrittura dell'immagine tabella: " + e.getMessage());
        }
    }
}