package dao.dto;

public class ClassDTO {
    private int releaseNumber; // Progressive integer
    private int numSmells;
    private int numMethods;
    private String path;
    private int loc;

    public ClassDTO(String name,
                    int releaseNumber,
                    int numSmells,
                    int numOps,
                    int loc)
    {
        this.path = name;
        this.releaseNumber = releaseNumber;
        this.numSmells = numSmells;
        this.numMethods = numOps;
        this.loc = loc;
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

    public int getLoc(){return loc;}
}