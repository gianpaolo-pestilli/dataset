package control;

import bean.ClassesBean;
import dao.ClassesDAO;
import dao.EffortClassesDAO;
import dao.SmellClassesDAO;
import exception.ConfigException;
import exception.ControllerException;
import settings.PropertiesSetter;

import java.util.List;

public class EffortRankingController extends RankingController{


    @Override
    protected void sortRanking(){
        List<ClassesBean> classes = ranking;

        for(ClassesBean c : classes){
            c.setDebt(null);
        }

        classes.sort((c1, c2) -> {
            int t1 = c1.getTimeSmell() != null ? c1.getTimeSmell() : 0;
            int t2 = c2.getTimeSmell() != null ? c2.getTimeSmell() : 0;
            return Integer.compare(t2, t1);
        });

        setRanking(classes);
    }

    @Override
    protected ClassesDAO getMyClassDAO(){
        return new EffortClassesDAO();
    }
}
