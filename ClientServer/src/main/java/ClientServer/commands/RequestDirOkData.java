package ClientServer.commands;

import ClientServer.FileInfo.FileInfo;

import java.io.Serializable;
import java.util.List;

public class RequestDirOkData implements Serializable {
    private final List<FileInfo> files;
    private String currentPath;

    public RequestDirOkData(List<FileInfo> files, String currentPath) {
        this.currentPath = currentPath;
        this.files = files;
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public String getNextPath() {
        return currentPath;
    }
}
