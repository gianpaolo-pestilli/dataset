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

    private ProportionDAO(){
        // Making it private
    }

    public static void writeProportion(double proportion) throws PersistenceException {
        try (FileWriter fw = new FileWriter(filename, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("PROPORTION = " + proportion + ";");
        } catch (IOException e) {
            throw new PersistenceException("Errore nella scrittura della proportion sul file: " + e.getMessage());
        }
    }

    public static void writePercentage(double percentage) throws PersistenceException {
        // Usiamo "true" nel costruttore di FileWriter per aggiungere (append) al file esistente
        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("PERCENTUALE CLASSI BUGGY = " + (percentage*100) + "%");
        } catch (IOException e) {
            throw new PersistenceException("Errore nella scrittura della percentuale sul file: " + e.getMessage());
        }
    }

    public static void writeFake(double fakes) throws PersistenceException {
        // Usiamo "true" nel costruttore di FileWriter per aggiungere (append) al file esistente
        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("PERCENTUALE BUG INESISTENTI: " + (fakes*100) + "% di tutti i ticket con commit;");
        } catch (IOException e) {
            throw new PersistenceException("Errore nella scrittura della percentuale sul file: " + e.getMessage());
        }
    }

    public static void writeInconsistent(double inc) throws PersistenceException {
        // Usiamo "true" nel costruttore di FileWriter per aggiungere (append) al file esistente
        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("Proportion necessaria per la IV del " + (inc*100) + "% di tutti i ticket con commit associato;");
        } catch (IOException e) {
            throw new PersistenceException("Errore nella scrittura della percentuale sul file: " + e.getMessage());
        }
    }


    public static void writeTicketWithNoCommit(double inc) throws PersistenceException {
        // Usiamo "true" nel costruttore di FileWriter per aggiungere (append) al file esistente
        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("Il " + (inc*100) + "% dei tickets non aveva un commit associato;");
        } catch (IOException e) {
            throw new PersistenceException("Errore nella scrittura della percentuale sul file: " + e.getMessage());
        }
    }





}
