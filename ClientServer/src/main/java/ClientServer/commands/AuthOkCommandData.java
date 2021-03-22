package ClientServer.commands;

import ClientServer.FileInfo;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

public class AuthOkCommandData implements Serializable {

    private final String username;
    private final String defPafh;
    private final List<FileInfo> files;

    public AuthOkCommandData(String username, List<FileInfo> files) {
        this.username = username;
        this.defPafh = "~";
        this.files = files;
    }

    public String getUsername() {
        return username;
    }
    public String getPath() {
        return defPafh;
    }

    public List<FileInfo> getFiles() {
        return files;
    }
}
