package dao.dto;

public class ClassDTO {
    private int releaseNumber; // Progressive integer
    private int numSmells;
    private int numMethods;
    private String path;

    public ClassDTO(String name,
                    int releaseNumber,
                    int numSmells,
                    int numOps)
    {
        this.path = name;
        this.releaseNumber = releaseNumber;
        this.numSmells = numSmells;
        this.numMethods = numOps;
    }

    public int getReleaseNumber(){
        return releaseNumber;
    }

    public int getNumSmells() {
        return numSmells;
    }

    public String getPath() {
        return path;
    }

    public int getNumMethods() {
        return numMethods;
    }
}