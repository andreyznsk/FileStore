package ClientServer.commands;

import java.io.Serializable;
import java.nio.channels.FileChannel;

public class FileSendCommandData implements Serializable {

    private final String fileName;
    private final String filePath;
    private final long fileParts;

    /**
     * Данные команды для отправки файла
     * @param fileName - название файла
     * @param filePath - путь клиента на сервере
     */
    public FileSendCommandData(String filePath, String fileName, long fileParts) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileParts = fileParts;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getFileParts() {
        return fileParts;
    }
}