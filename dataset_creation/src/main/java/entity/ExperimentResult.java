package entity;

public class ExperimentResult {
    public String cut, selection, balancing, validation, classifier;
    public double acc, prec, rec, auc, kap;
    public int tp, fp, tn, fn;

    public ExperimentResult(String[] v) {
        this.cut = v[0]; this.selection = v[1]; this.balancing = v[2];
        this.validation = v[3]; this.classifier = v[4];
        this.acc = Double.parseDouble(v[5]); this.prec = Double.parseDouble(v[6]);
        this.rec = Double.parseDouble(v[7]); this.auc = Double.parseDouble(v[8]);
        this.kap = Double.parseDouble(v[9]);
        this.tp = Integer.parseInt(v[10]); this.fp = Integer.parseInt(v[11]);
        this.tn = Integer.parseInt(v[12]); this.fn = Integer.parseInt(v[13]);
    }
}
