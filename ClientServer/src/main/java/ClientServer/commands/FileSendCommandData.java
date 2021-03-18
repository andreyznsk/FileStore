package ClientServer.commands;

import java.io.Serializable;
import java.nio.channels.FileChannel;

public class FileSendCommandData implements Serializable {

    private final String fileName;

    public FileSendCommandData(String fileChannel)  {
        this.fileName = fileChannel;
    }

    public String getFileName() {
        return fileName;
    }
}