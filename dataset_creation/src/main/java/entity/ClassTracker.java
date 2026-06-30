package entity;

import java.util.HashSet;
import java.util.Set;

/**
 * Questa classe funge da "Registro Storico Globale".
 * Sopravvive per tutto il ciclo di analisi e accumula i dati
 * dal primo giorno di vita del progetto fino all'ultima release.
 */
public class ClassTracker {
    private int numRev = 0;
    private int numFix = 0;
    private Set<String> authors = new HashSet<>();
    private int churn = 0;
    private int maxLocAdded = 0;
    private int totalLocAdded = 0;
    private int totalLocDeleted = 0;
    private int totalChangeSet = 0;
    private int maxChangeSet = 0;

    // Incrementa il contatore dei commit totali (Metrica 3)
    public void incrementNumRev() { this.numRev++; }

    // Incrementa il contatore dei fix storici (Metrica 5)
    public void incrementNumFix() { this.numFix++; }

    // Salva l'autore in un Set. Il Set ignora in automatico i duplicati (Metrica 7)
    public void addAuthor(String authorEmail) { this.authors.add(authorEmail); }

    // Riceve dal Controller le righe aggiunte e rimosse nel singolo commit
    public void addChurn(int added, int deleted) {
        this.churn += (added + deleted); // Churn totale (Metrica 9)
        this.totalLocAdded += added;     // Serve per la media LOC storiche (Metrica 13)
        this.totalLocDeleted += deleted; // Serve per calcolare la Size/LOC finale (Metrica 1)
        this.maxLocAdded = Math.max(this.maxLocAdded, added); // Salva il picco massimo (Metrica 11)
    }

    // Riceve dal Controller quanti file sono stati modificati assieme a questo
    public void addChangeSet(int size) {
        this.totalChangeSet += size; // Serve per la media del ChangeSet storico (Metrica 15)
        this.maxChangeSet = Math.max(this.maxChangeSet, size); // Salva il picco massimo (Metrica 17)
    }

    // Getters standard per permettere all'entità di copiare questi valori
    public int getNumRev() { return numRev; }
    public int getNumFix() { return numFix; }
    public int getAuthorsCount() { return authors.size(); }
    public int getChurn() { return churn; }
    public int getMaxLocAdded() { return maxLocAdded; }
    public int getTotalLocAdded() { return totalLocAdded; }
    public int getTotalLocDeleted() { return totalLocDeleted; }
    public int getTotalChangeSet() { return totalChangeSet; }
    public int getMaxChangeSet() { return maxChangeSet; }
}