package ClientServer.commands;

import java.io.Serializable;
import java.nio.channels.FileChannel;

public class FileSendCommandData implements Serializable {

    private final String fileName;
    private final String filePath;

    /**
     * Данные команды для отправки файла
     * @param fileName - название файла
     * @param filePath - путь клиента на сервере
     */
    public FileSendCommandData(String filePath, String fileName) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }
}