package entity;

public class ExperimentResult {
    public String cut;
    public String selection;
    public String balancing;
    public String validation;
    public String classifier;

    public double acc;
    public double prec;
    public double rec;
    public double auc;
    public double kap;
    public int tp;
    public int  fp;
    public int tn;
    public int  fn;

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
