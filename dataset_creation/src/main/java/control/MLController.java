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



        userBoundary.printMessage(new MessageBean("Trovati " + classifiers.size() + " esperimenti da lanciare. Avvio " + numThreads + " thread..."));


        List<Future<?>> futures = new ArrayList<>();
        String name = MLDatasetDAO.getDataset();


        try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {


            for (Classifier cls : classifiers) {


                futures.add(executor.submit(() -> {


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


            executor.shutdown();


            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ControllerException("Attesa dei thread interrotta: " + e.getMessage());
                } catch (ExecutionException e) {
                    throw new ControllerException("Errore critico durante l'esecuzione del task: " + e.getMessage());
                }
            }
        }

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