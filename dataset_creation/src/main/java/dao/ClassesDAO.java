package dao;

import bean.ClassesBean;
import exception.PersistenceException;

import java.util.List;

public abstract class ClassesDAO {

    public abstract void saveRanking(List<ClassesBean> classList) throws PersistenceException;

    public abstract String getOutputFile() throws PersistenceException;
}
