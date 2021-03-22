package ClientServer.FileInfo;


public enum FileType{
    FILE("F"), DIRECTORY("D");

    private String name;

   FileType(String name) {
        this.name = name;
       System.out.println(name);
    }

    public String getName() {
        return name;
    }
}