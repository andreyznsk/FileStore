package ClientServer.commands;

import java.nio.Buffer;

public class FileDataCommand {
    private String file;
    private long startPosition;
    private byte[] data;

    public FileDataCommand(long filePosition, byte[] data) {
        this.startPosition = filePosition;
        this.data = data;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
