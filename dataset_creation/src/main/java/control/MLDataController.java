package control;

import bean.MessageBean;
import dao.DatasetDAO;
import entity.Class;
import entity.Release;
import exception.ControllerException;
import exception.PersistenceException;
import settings.DataType;

import java.util.List;

public class MLDataController extends AppController {

    private List<Release> allDataset;         // Dataset A: Originale
    private List<Release> smelledDataset;     // Dataset B+: Solo classi con #smells > 0
    private List<Release> notSmelledDataset;  // Dataset C: Solo classi con #smells == 0
    private List<Release> cleanedDataset;     // Dataset B: Sintetico (B+ con smells azzerati)

    @Override
    public void start() throws ControllerException {
        try {
            // IL "TRUCCO" DEL DAO: Leggiamo il file 4 volte per istanziare
            // 4 alberi di oggetti Release/Class completamente slegati in memoria.
            this.allDataset = DatasetDAO.getOfficialDataset();
            this.smelledDataset = DatasetDAO.getOfficialDataset();
            this.notSmelledDataset = DatasetDAO.getOfficialDataset();
            this.cleanedDataset = DatasetDAO.getOfficialDataset();

            // Ora applichiamo i filtri in totale sicurezza
            filterBPlus();
            filterC();
            createDatasetB();

        } catch (PersistenceException e) {
            throw new ControllerException("Errore durante la manipolazione dei dataset: " + e.getMessage());
        }
    }

    @Override
    public void finish() throws ControllerException {
        userBoundary.printMessage(new MessageBean("Writing dataset variations for What-If Analysis..."));
        try {
            // Scrive i file CSV pronti per Weka
            DatasetDAO.writeWhichDataset(smelledDataset, DataType.B_PLUS);
            DatasetDAO.writeWhichDataset(notSmelledDataset, DataType.C);
            DatasetDAO.writeWhichDataset(cleanedDataset, DataType.B);

            userBoundary.printMessage(new MessageBean("Datasets generati con successo!"));
        } catch (PersistenceException e) {
            throw new ControllerException("Errore di scrittura: " + e.getMessage());
        }
    }

    /**
     * Genera il Dataset B+: Rimuove tutte le classi senza smell.
     * Rimangono solo le classi con NSmells > 0.
     */
    private void filterBPlus() {
        for (Release rel : smelledDataset) {
            List<Class> classes = rel.getClasses();
            classes.removeIf(c -> c.getNumSmells() == 0);
        }
    }

    /**
     * Genera il Dataset C: Rimuove tutte le classi con smell.
     * Rimangono solo le classi nate immacolate (NSmells == 0).
     */
    private void filterC() {
        for (Release rel : notSmelledDataset) {
            List<Class> classes = rel.getClasses();
            classes.removeIf(c -> c.getNumSmells() > 0);
        }
    }

    /**
     * Genera il Dataset B: Il dataset fittizio/sintetico.
     * Parte concettualmente da B+, ma forza la feature smell a zero.
     */
    private void createDatasetB() {
        for (Release rel : cleanedDataset) {
            List<Class> classes = rel.getClasses();

            // 1. Isola le classi che in natura presentavano smell (come B+)
            classes.removeIf(c -> c.getNumSmells() == 0);

            // 2. Manipolazione What-If: azzera forzatamente la metrica
            for (Class c : classes) {
                c.setNumSmells(0);
            }
        }
    }
}