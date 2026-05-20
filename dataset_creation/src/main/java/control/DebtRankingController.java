package control;

import bean.ClassesBean;
import dao.ClassesDAO;
import dao.DebtClassesDAO;

import java.util.List;

public class DebtRankingController extends RankingController{

    @Override
    protected void sortRanking(){
        List<ClassesBean> classes = ranking;

        classes.sort((c1, c2) -> {
            int t1 = c1.getDebt() != null ? c1.getDebt() : 0;
            int t2 = c2.getDebt() != null ? c2.getDebt() : 0;
            return Integer.compare(t2, t1);
        });

        setRanking(classes);
    }

    @Override
    protected ClassesDAO getMyClassDAO(){
        return new DebtClassesDAO();
    }

}
