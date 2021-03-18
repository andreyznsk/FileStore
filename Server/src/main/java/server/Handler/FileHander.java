package server.Handler;

import ClientServer.FileInfo;
import ClientServer.FileInfoBuiled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileHander {
    public static List<FileInfo> getFilesInfo(String path){
        String serverPath = "!ServerDisc/" + path;
        Path srvPath = Paths.get(serverPath);
        try {
            if (!Files.isExecutable(srvPath)) Files.createDirectory(srvPath);
            List<FileInfo> files = Files.list(srvPath).map(FileInfoBuiled::infoBuilder).collect(Collectors.toList());
            return files;
        } catch (IOException e) {
            e.printStackTrace();
        }
       return null;
    }

    public static boolean isSrvDirectory(String path){
        String serverPath = "!ServerDisc/" + path;
        Path srvPath = Paths.get(serverPath);
        return Files.isDirectory(srvPath);
    }

    public static Path getPathByCurrentDir(String path) {
        String serverPath = "!ServerDisc/" + path;
        System.out.println("ServerPath is : " + serverPath);
        if(Files.isDirectory(Paths.get(serverPath))) {
            try {
                Path p = Paths.get(serverPath);
                return p;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }

}
