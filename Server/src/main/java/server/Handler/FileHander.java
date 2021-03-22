package server.Handler;

import ClientServer.FileInfo.FileInfo;
import ClientServer.FileInfo.FileInfoBuiled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileHander {

    private final static String SERVER_PATH = "!ServerDisc/";

    /**
     * Метод собирает коллекцию(лист) объектов класса FileInfo
     * @param path - путь по которому нужно собрать информацию по файлам и папкам
     * @return - возвращает ссылку на коллекцию
     */
    public static List<FileInfo> getFilesInfo(String path){
        String serverPath = SERVER_PATH + path;
        Path srvPath = Paths.get(serverPath);
        try {
            if (!Files.isExecutable(srvPath)) Files.createDirectory(srvPath);
            List<FileInfo> files = Files.list(srvPath).map(FileInfoBuiled::infoBuilder).collect(Collectors.toList());
            return files;
        } catch (IOException e) {
            e.printStackTrace();
        }
       return null;
    }

    public static boolean isSrvDirectory(String path){
        String serverPath = SERVER_PATH + path;
        Path srvPath = Paths.get(serverPath);
        return Files.isDirectory(srvPath);
    }

    /**
     * Метод формирования пути к папке и файлу клиента, принимаеи на вход /папка клиента/.../вложенные папки
     * @param path - относительный путь к папке клиента
     * @return - объект класса Path к файлу/папке клиента на сервере
     */
    public static Path getPathByCurrentDir(String path) {
        String serverPath = SERVER_PATH + path;
        System.out.println("Путь к папке сервера и файлу : " + serverPath);
        if(Files.isDirectory(Paths.get(serverPath))) {
            try {
                Path p = Paths.get(serverPath);
                return p;
            } catch (Exception e) {
                System.err.println("Неправильная дирректория файла");
                e.printStackTrace();
            }

        }
        return null;
    }

    /**
     * Метод проверки существования дирректории пользователя на сервере.
     * Если зарегистрировался новыйв польователь, а папки нет, то создать исходную папку
     * @param path - относительный путь к папке клиента
     */
    public static void isSrvDirectoryExist(String path) {
        String serverPath = SERVER_PATH + path;
        Path p = Paths.get(serverPath);
        try {
            if (!Files.isDirectory(p)) Files.createDirectory(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
