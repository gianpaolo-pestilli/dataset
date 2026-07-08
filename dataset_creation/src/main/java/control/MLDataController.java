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

    private List<Release> allDataset;         // Dataset A
    private List<Release> smelledDataset;     // Dataset B+
    private List<Release> notSmelledDataset;  // Dataset C
    private List<Release> cleanedDataset;     // Dataset B

    @Override
    public void start() throws ControllerException {
        try {

            this.allDataset = DatasetDAO.getOfficialDataset();
            this.smelledDataset = DatasetDAO.getOfficialDataset();
            this.notSmelledDataset = DatasetDAO.getOfficialDataset();
            this.cleanedDataset = DatasetDAO.getOfficialDataset();

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

            DatasetDAO.writeWhichDataset(smelledDataset, DataType.B_PLUS);
            DatasetDAO.writeWhichDataset(notSmelledDataset, DataType.C);
            DatasetDAO.writeWhichDataset(cleanedDataset, DataType.B);

            userBoundary.printMessage(new MessageBean("Datasets generati con successo!"));
        } catch (PersistenceException e) {
            throw new ControllerException("Errore di scrittura: " + e.getMessage());
        }
    }


    private void filterBPlus() {
        for (Release rel : smelledDataset) {
            List<Class> classes = rel.getClasses();
            classes.removeIf(c -> c.getNumSmells() == 0);
        }
    }


    private void filterC() {
        for (Release rel : notSmelledDataset) {
            List<Class> classes = rel.getClasses();
            classes.removeIf(c -> c.getNumSmells() > 0);
        }
    }


    private void createDatasetB() {
        for (Release rel : cleanedDataset) {
            List<Class> classes = rel.getClasses();


            classes.removeIf(c -> c.getNumSmells() == 0);

            for (Class c : classes) {
                c.setNumSmells(0);
            }
        }
    }

    public List<Release> getAllDataset(){
        return allDataset;
    }
    public List<Release> getSmelledDataset(){
        return smelledDataset;
    }
    public List<Release> getNotSmelledDataset(){
        return notSmelledDataset;
    }
    public List<Release> getCleanedDataset(){
        return cleanedDataset;
    }

}