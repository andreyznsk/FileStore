package ClientServer.FileInfo;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FileInfoBuiled {
    public static FileInfo infoBuilder(Path path) {//Класс собирает инфорамаю по пути
        //Данный клас написан отдельно, т.к. содержит обект Path данный объект пока не получилось сеарилизовать

        try {
            String fileName = path.getFileName().toString();
            long size = Files.size(path);
            FileType type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (type == FileType.DIRECTORY) {
                size = -1L;
            }
            LocalDateTime lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(0));

            return new FileInfo(fileName, type, size, lastModified);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info");
        }
    }
}
