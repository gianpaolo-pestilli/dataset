package entity;

public class MetricContainer {
    private String metricName;
    private double meanA;
    private double meanB;
    private double meanB_plus;
    private double meanC;
    private double corrWithDefects;
    private double corrWithSmells;
    private boolean importantForDefects;
    private boolean importantForSmells;

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public double getMeanA() {
        return meanA;
    }

    public void setMeanA(double meanA) {
        this.meanA = meanA;
    }

    public double getMeanB() {
        return meanB;
    }

    public void setMeanB(double meanB) {
        this.meanB = meanB;
    }

    public double getMeanB_plus() {
        return meanB_plus;
    }

    public void setMeanB_plus(double meanB_plus) {
        this.meanB_plus = meanB_plus;
    }

    public double getMeanC() {
        return meanC;
    }

    public void setMeanC(double meanC) {
        this.meanC = meanC;
    }

    public double getCorrWithDefects() {
        return corrWithDefects;
    }

    public void setCorrWithDefects(double corrWithDefects) {
        this.corrWithDefects = corrWithDefects;
    }

    public double getCorrWithSmells() {
        return corrWithSmells;
    }

    public void setCorrWithSmells(double corrWithSmells) {
        this.corrWithSmells = corrWithSmells;
    }

    public boolean isImportantForDefects() {
        return importantForDefects;
    }

    public void setImportantForDefects(boolean importantForDefects) {
        this.importantForDefects = importantForDefects;
    }

    public boolean isImportantForSmells() {
        return importantForSmells;
    }

    public void setImportantForSmells(boolean importantForSmells) {
        this.importantForSmells = importantForSmells;
    }
}