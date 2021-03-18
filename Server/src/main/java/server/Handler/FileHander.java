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
        try {
            List<FileInfo> files = Files.list(Paths.get(path)).map(FileInfoBuiled::infoBuilder).collect(Collectors.toList());
            return files;
        } catch (IOException e) {
            e.printStackTrace();
        }
       return null;
    }
}
