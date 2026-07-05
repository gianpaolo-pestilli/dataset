package control;

import bean.MessageBean;
import dao.MLDatasetDAO;
import entity.Classifier;
import entity.WekaWorker;
import exception.ControllerException;
import settings.ExperimentGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MLController extends AppController{

    private List<Classifier> classifiers;

    private int numThreads = 6; // For my machine it's the best

    @Override
    public void start() throws ControllerException {

        this.classifiers = ExperimentGenerator.generateClassifiers();

        // We have all the experiments now

        userBoundary.printMessage(new MessageBean("Trovati " + classifiers.size() + " esperimenti da lanciare. Avvio " + numThreads + " thread..."));

        // 2. Creiamo il "Pool" (la squadra) di 6 operai
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Questa lista ci serve per tenere traccia dello stato di avanzamento di ogni singolo task
        List<Future<?>> futures = new ArrayList<>();

        // 3. Buttiamo tutti gli esperimenti nella cesta (la coda dell'executor)
        for (Classifier cls : classifiers) {

            // submit() dice al Pool: "Appena hai un thread libero, fagli eseguire questo blocco di codice"
            futures.add(executor.submit(() -> {

                // --- DA QUI IN POI SIAMO IN UN THREAD PARALLELO ---
                try {
                    long startTime = System.currentTimeMillis();

                    WekaWorker weka = new WekaWorker(cls);
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

        // 5. Il thread principale (Main) ora deve ASPETTARE che tutti i 6 operai abbiano finito
        for (Future<?> f : futures) {
            try {
                f.get(); // Il metodo get() "blocca" l'esecuzione finché quel task specifico non è terminato
            } catch (Exception e) {
                throw new ControllerException("Errore critico durante l'attesa dei thread: " + e.getMessage());
            }
        }

        userBoundary.printMessage(new MessageBean("Tutti gli esperimenti di Machine Learning sono terminati con successo!"));

    }

    @Override
    public void finish() throws ControllerException {

        MLDatasetDAO.writeResults(this.classifiers);

    }


}
