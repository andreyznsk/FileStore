package ClientServer.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class FileInfo implements Serializable {

    private final String fileName;
    private final FileType type;
    private final long size;
    private final LocalDateTime lastModified;

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

