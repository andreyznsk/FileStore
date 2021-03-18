package ClientServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FileInfoBuiled {
    public static FileInfo infoBuilder(Path path) {
        try {
            String fileName = path.getFileName().toString();
            long size = Files.size(path);
            FileInfo.FileType type = Files.isDirectory(path) ? FileInfo.FileType.DIRECTORY : FileInfo.FileType.FILE;
            if (type == FileInfo.FileType.DIRECTORY) {
                size = -1L;
            }
            LocalDateTime lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(0));

            return new FileInfo(fileName, type, size, lastModified);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info");
        }
    }
}
