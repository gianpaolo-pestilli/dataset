package entity;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.util.*;
import java.util.function.ToDoubleFunction;

public class CorrelationAnalyzer {

    private static final double SIGNIFICANCE_THRESHOLD = 0.05;

    // Nome metrica -> come estrarla da un oggetto Class.
    // Include tutte le feature numeriche disponibili in Class.java,
    // sia la variante "in release" sia la variante "FromBegin".
    private static final Map<String, ToDoubleFunction<Class>> METRICS = new LinkedHashMap<>();
    static {
        METRICS.put("LOC", Class::getLoc);

        METRICS.put("numRevisions", Class::getNumRevisions);
        METRICS.put("numRevisionsFromBegin", Class::getNumRevisionsFromBegin);

        METRICS.put("numFixes", Class::getNumFixes);
        METRICS.put("numFixesFromBegin", Class::getNumFixesFromBegin);

        METRICS.put("numAuthors", Class::getNumAuthors);
        METRICS.put("numAuthorsFromBegin", Class::getNumAuthorsFromBegin);

        METRICS.put("churn", Class::getChurn);
        METRICS.put("churnFromBegin", Class::getChurnFromBegin);

        METRICS.put("maxLOCAdded", Class::getMaxLOCAdded);
        METRICS.put("maxLOCAddedFromBegin", Class::getMaxLOCAddedFromBegin);

        METRICS.put("avgLOCAdded", Class::getAvgLOCAdded);
        METRICS.put("avgLOCAddedFromBegin", Class::getAvgLOCAddedFromBegin);

        METRICS.put("avgChangeSet", Class::getAvgChangeSet);
        METRICS.put("avgChangeSetFromBegin", Class::getAvgChangeSetFromBegin);

        METRICS.put("maxChangeSet", Class::getMaxChangeSet);
        METRICS.put("maxChangeSetFromBegin", Class::getMaxChangeSetFromBegin);

        METRICS.put("age", Class::getAge);
        METRICS.put("weightedAge", Class::getWeightedAge);

        METRICS.put("numOps", Class::getNumOps);
        METRICS.put("avgTimeBetweenCommits", Class::getAvgTimeBetweenCommits);

        METRICS.put("numSmells", Class::getNumSmells);
    }

    /**
     * @param datasetA     Dataset A completo
     * @param datasetBPlus Dataset B+ (solo classi con NSmells > 0)
     * @param datasetB     Dataset B sintetico (B+ con NSmells forzato a 0)
     * @param datasetC     Dataset C (solo classi con NSmells == 0)
     */
    public List<MetricContainer> computeTable(List<Release> datasetA,
                                              List<Release> datasetBPlus,
                                              List<Release> datasetB,
                                              List<Release> datasetC) {

        List<Class> classesA = flatten(datasetA);
        List<Class> classesBPlus = flatten(datasetBPlus);
        List<Class> classesB = flatten(datasetB);
        List<Class> classesC = flatten(datasetC);

        double[] nSmellsA = extract(classesA, Class::getNumSmells);
        double[] defectivenessA = extractDefectiveness(classesA);

        List<MetricContainer> containers = new ArrayList<>();

        for (Map.Entry<String, ToDoubleFunction<Class>> entry : METRICS.entrySet()) {
            String metricName = entry.getKey();
            ToDoubleFunction<Class> extractor = entry.getValue();

            double[] valuesA = extract(classesA, extractor);

            MetricContainer container = new MetricContainer();
            container.setMetricName(metricName);
            container.setMeanA(mean(classesA, extractor));
            container.setMeanB_plus(mean(classesBPlus, extractor));
            container.setMeanB(mean(classesB, extractor));
            container.setMeanC(mean(classesC, extractor));

            if (metricName.equals("numSmells")) {
                container.setCorrWithSmells(0.0);
                container.setImportantForSmells(false); // N/A: metrica correlata con se stessa
            } else {
                double[] rp = pearsonAndPValue(valuesA, nSmellsA);
                container.setCorrWithSmells(rp[0]);
                container.setImportantForSmells(rp[1] < SIGNIFICANCE_THRESHOLD);
            }

            double[] rpDefect = pearsonAndPValue(valuesA, defectivenessA);
            container.setCorrWithDefects(rpDefect[0]);
            container.setImportantForDefects(rpDefect[1] < SIGNIFICANCE_THRESHOLD);

            containers.add(container);
        }

        return containers;
    }

    private List<Class> flatten(List<Release> releases) {
        List<Class> result = new ArrayList<>();
        for (Release r : releases) result.addAll(r.getClasses());
        return result;
    }

    private double mean(List<Class> classes, ToDoubleFunction<Class> extractor) {
        return classes.stream().mapToDouble(extractor).average().orElse(0.0);
    }

    private double[] extract(List<Class> classes, ToDoubleFunction<Class> extractor) {
        return classes.stream().mapToDouble(extractor).toArray();
    }

    private double[] extractDefectiveness(List<Class> classes) {
        double[] result = new double[classes.size()];
        for (int i = 0; i < classes.size(); i++) {
            result[i] = classes.get(i).isBuggy() ? 1.0 : 0.0;
        }
        return result;
    }

    private double[] pearsonAndPValue(double[] x, double[] y) {
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double r = pc.correlation(x, y);

        int n = x.length;
        double t = r * Math.sqrt(n - 2.0) / Math.sqrt(1 - r * r);
        TDistribution tDist = new TDistribution(n - 2.0);
        double pValue = 2 * (1 - tDist.cumulativeProbability(Math.abs(t)));

        return new double[]{r, pValue};
    }
}