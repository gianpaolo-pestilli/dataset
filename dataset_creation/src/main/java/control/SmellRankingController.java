package control;

import bean.ClassesBean;
import dao.ClassesDAO;
import dao.SmellClassesDAO;
import java.util.List;

public class SmellRankingController extends RankingController {

    @Override
    protected void sortRanking(){
        List<ClassesBean> classes = ranking;
        classes.sort((c1, c2) -> c2.getNumSmell().compareTo(c1.getNumSmell()));
        for(ClassesBean c : classes){
            c.setTimeSmell(null);
            c.setDebt(null);
        }
        setRanking(classes);
    }
    @Override
    protected ClassesDAO getMyClassDAO(){
        return new SmellClassesDAO();
    }
}
