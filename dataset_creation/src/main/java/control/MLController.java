package control;

import bean.MessageBean;
import dao.MLDatasetDAO;
import entity.Classifier;
import entity.WekaWorker;
import exception.ControllerException;
import exception.PersistenceException;
import settings.ExperimentGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MLController extends AppController{

    private List<Classifier> classifiers;

    private int numThreads = 3; // For my machine it's the best

    @Override
    public void start() throws ControllerException {

        this.classifiers = ExperimentGenerator.generateClassifiers();

        // We have all the experiments now

        userBoundary.printMessage(new MessageBean("Trovati " + classifiers.size() + " esperimenti da lanciare. Avvio " + numThreads + " thread..."));

        // Questa lista ci serve per tenere traccia dello stato di avanzamento di ogni singolo task
        List<Future<?>> futures = new ArrayList<>();
        String name = MLDatasetDAO.getDataset();

        // 2. Creiamo il "Pool" (la squadra) usando il try-with-resources (supportato per ExecutorService da Java 19+)
        try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {

            // 3. Buttiamo tutti gli esperimenti nella cesta (la coda dell'executor)
            for (Classifier cls : classifiers) {

                // submit() dice al Pool: "Appena hai un thread libero, fagli eseguire questo blocco di codice"
                futures.add(executor.submit(() -> {

                    // --- DA QUI IN POI SIAMO IN UN THREAD PARALLELO ---
                    try {
                        long startTime = System.currentTimeMillis();

                        WekaWorker weka = new WekaWorker(cls, name);
                        // Technically, it has to be a boundary responsibility, but we don't have "time" to instantiate a useless bean
                        weka.run();

                        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                        userBoundary.printMessage(new MessageBean("OK -> Esperimento completato in " + elapsed + "s dal thread: " + Thread.currentThread().getName()));
                        return null;

                    } catch (Exception e) {
                        throw new ControllerException("Errore critico durante l'esecuzione dell'esperimento: " + e.getMessage());
                    }

                }));
            }

            // 4. Diciamo all'Executor che non aggiungeremo altri esperimenti alla cesta
            executor.shutdown();

            // 5. Il thread principale (Main) ora deve ASPETTARE che tutti i worker abbiano finito
            for (Future<?> f : futures) {
                try {
                    f.get(); // Il metodo get() "blocca" l'esecuzione finché quel task specifico non è terminato
                } catch (InterruptedException e) {
                    // Ripristino dello stato di interruzione del thread richiesto da Sonar
                    Thread.currentThread().interrupt();
                    throw new ControllerException("Attesa dei thread interrotta: " + e.getMessage());
                } catch (ExecutionException e) {
                    throw new ControllerException("Errore critico durante l'esecuzione del task: " + e.getMessage());
                }
            }
        } // Alla fine di questo blocco, il try-with-resources si assicura di chiamare close() sull'executor

        userBoundary.printMessage(new MessageBean("Tutti gli esperimenti di Machine Learning sono terminati con successo!"));

    }

    @Override
    public void finish() throws ControllerException {
        try {
            MLDatasetDAO.writeResults(this.classifiers);
        } catch (PersistenceException e) {
            throw new ControllerException(e.getMessage());
        }
    }
}