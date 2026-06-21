package control;

import bean.ClassesBean;
import bean.MessageBean;
import dao.ApplicativeRankingDAO;
import dao.ClassesDAO;
import dao.SmellClassesDAO;
import exception.ControllerException;
import entity.Class;
import exception.PersistenceException;

import java.util.ArrayList;
import java.util.List;

public class ApplicativeRankingController extends AppController {

    List<ClassesBean> classes = new ArrayList<>(); // Give them to the DAO that writes

    @Override
    public void start() throws ControllerException{
        //Reading from all the classes
        try {
            SmellClassesDAO dao = new SmellClassesDAO();
            List<Class> readClasses = dao.getAllClasses();
            this.classes = convert(readClasses);
        } catch (PersistenceException e) {
            throw new ControllerException(e.getMessage());
        }
        filterApplicativeJavaClasses();
    }

    @Override
    public void finish() throws ControllerException {
        // Writing on file...
        userBoundary.printMessage(new MessageBean("Classes have been filtered successfully..."));
        ClassesDAO dao = new ApplicativeRankingDAO();
        try{
            dao.saveRanking(this.classes);
        } catch (PersistenceException e) {
            throw new ControllerException(e.getMessage());
        }
        userBoundary.printMessage(new MessageBean("DONE"));
    }



    private ClassesBean convertToBean(Class c) {
        return new ClassesBean(c.getName(), c.getNumSmells(),null);
    }

    private List<ClassesBean> convert(List<Class> classes){
        List<ClassesBean> beans = new ArrayList<>();
        for (Class c : classes) {
            beans.add(convertToBean(c));
        }
        return beans;
    }

    private void filterApplicativeJavaClasses() {
        List<ClassesBean> filtered = new ArrayList<>();
        for (ClassesBean c : this.classes) {
            String path = c.getClassName();
            if (path == null) {
                continue;
            }
            // Needs to be a java-file // We know that but it's to be sure
            boolean isJava = path.endsWith(".java");

            // Technically we have filtered this yet, but it is to be sure
            boolean isNotTest = !path.contains("src/test/")
                    && !path.endsWith("Test.java")
                    && !path.endsWith("IT.java");

            // core and common contain the most applicative classes
            boolean isCoreOrCommon = path.contains("/core/") || path.startsWith("core/")
                    || path.contains("/common/") || path.startsWith("common/");

            // we don't want simple calls to DB, persistence or console
            boolean isNotDB = !path.contains("/persistence") && !path.contains("/dao/");
            boolean isNotUI = !path.contains("/console/") && !path.contains("/ui/");
            // we don't want exceptions
            boolean isNotException = !path.contains("Exception");
            boolean hasSomeSmell = c.getNumSmell()> 0;

            if (isJava && isNotTest && isCoreOrCommon && isNotDB && isNotUI&& isNotException && hasSomeSmell) {
                filtered.add(c);
            }
        }

        this.classes = filtered;
    }
}
