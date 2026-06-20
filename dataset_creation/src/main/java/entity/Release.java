package entity;

import dao.ReleaseDAO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Release {

        private String projectName;
        private LocalDate releaseDate;
        private String releaseID;
        private String version;

        private int progressiveNumber; // Useful for Proportion

        private long age;
        private List<Class> classes = new ArrayList<>();

        public Release(String projectName,
                       LocalDate releaseDate,
                       String releaseID,
                       String version){
            this.projectName = projectName;
            this.releaseDate = releaseDate;
            this.releaseID = releaseID;
            this.version = version;

        }

        public String getProjectName(){
        return projectName;
    }
        public LocalDate getReleaseDate(){
        return releaseDate;
    }
        public String getID(){
            return releaseID;
        }
        public String getVersion(){return version;}

        public void addClass(String classname){
            Class newClass = new Class(this, classname);
            classes.add(newClass);
        }

        public void setAge(long age){
            this.age = age;
        }

        public long getAge(){
            return age;
        }

        public List<Class> getClasses(){
            return classes;
        }

        public void setProgressiveNumber(int x){
            progressiveNumber = x;
        }

        public int getProgressiveNumber(){return progressiveNumber;}

    }


