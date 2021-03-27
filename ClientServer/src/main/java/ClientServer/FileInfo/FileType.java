package ClientServer.FileInfo;


public enum FileType{//Как вы писали в замечаниях выынес в отдельный класс
    FILE("F"), DIRECTORY("D");

    private String name;

   FileType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}