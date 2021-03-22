package ClientServer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class FileInfo implements Serializable {
    public enum FileType{
        FILE("F"), DIRECTORY("D");

        private String name;

        FileType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private String fileName;
    private FileType type;
    private long size;
    private LocalDateTime lastModified;

    public String getFileName() {
        return fileName;
    }

    public FileType getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public FileInfo(String fileName, FileType type, long size, LocalDateTime lastModified) {
        this.fileName = fileName;
        this.type = type;
        this.size = size;
        this.lastModified = lastModified;

    }
}

