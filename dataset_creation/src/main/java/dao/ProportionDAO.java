package dao;

import exception.PersistenceException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ProportionDAO {
    /*
    Write on a file:
    1. Proportion value
    2. % buggy classes
    */
    private static String filename = "proportion_info.txt";

    public static void writeProportion(double proportion) throws PersistenceException {
        try (FileWriter fw = new FileWriter(filename, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("PROPORTION = " + proportion);
        } catch (IOException e) {
            throw new PersistenceException("Errore nella scrittura della proportion sul file: " + e.getMessage());
        }
    }

    public static void writePercentage(double percentage) throws PersistenceException {
        // Usiamo "true" nel costruttore di FileWriter per aggiungere (append) al file esistente
        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("PERCENTUALE CLASSI BUGGY = " + percentage + " %");
        } catch (IOException e) {
            throw new PersistenceException("Errore nella scrittura della percentuale sul file: " + e.getMessage());
        }
    }
}
