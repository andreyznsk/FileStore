package ru.geekbrains.fileserver_netty.Handler;

import ClientServer.Command;
import ClientServer.FileInfo.FileInfo;
import ClientServer.FileInfo.FileInfoBuiled;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static ClientServer.Command.*;

public class FileHander {

    private final static String SERVER_PATH = "!ServerDisc/";
    private final static int BUFFER_SIZE=1024;

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

    /**
     * Метод проверяет существует ли папка на сервере
     * @param path
     * @return
     */
    public static boolean isSrvDirectory(String path){
        String serverPath = SERVER_PATH + path;
        Path srvPath = Paths.get(serverPath);
        return Files.isDirectory(srvPath);
    }

    /**
     * Метод формирования пути к папке и файлу клиента, принимаеи на вход /папка клиента/.../вложенные папки
     * @param path - относительный путь к папке клиента
     * @return Path - объект класса Path к файлу/папке клиента на сервере
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

    /**
     * Статический мтод получения файла от клиента на сервер
      * @param userPath - в какую дирректорию положить файл на сервере
     * @param fileName - имя файла
     * @param currrentUserDir - текущая дирректория клиента
     * @param client - сокет канал
     * @param nickname - ник
     * @param clientHandler - обработчик текущего клиента
     * @param fileParts - количество частей файла которые необходимо получить
     */
    public static void receiveFile(String userPath, String fileName, String currrentUserDir, ChannelHandlerContext client, String nickname, ClientHandler clientHandler, long fileParts){
            Path p = FileHander.getPathByCurrentDir(userPath);// Формируем путь к каталогу на сервере
            String userServerPath = p + "\\" + fileName;
            System.out.println("Fiile recive: " + userServerPath);
            try {
                clientHandler.sendCommand(requestTransmiterOk());//Отправляем команду клиенту, что отдельный поток поднят и ждет получения файла
                RandomAccessFile aFile;
                try {
                    aFile = new RandomAccessFile(userServerPath, "rw");
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    FileChannel fileChannel = aFile.getChannel();

                 /*   while (fileParts > 0) {//Читаем из сокета пока все части файла не получим
                        while (client.read(buffer) > 0) {
                            buffer.flip();
                            fileChannel.write(buffer);
                            buffer.clear();
                            fileParts--;
                        }
                    }
*/
                    fileChannel.close();
                    System.out.println("End of file reached..Closing channel");

                } catch (IOException e) {
                    if (fileParts!=0) System.err.println("Fail receive failed!!!");
                    e.printStackTrace();
                    client.close();
                }
                //После того как приняли файл посылаем клиенту команду на обновление списка файлов в текщей папке
                List<FileInfo> files;
                String currrentUserDirTmp = nickname + getUserPath(currrentUserDir);// добавляем каталог пользователя к запрашиваемому каталогу
                files = FileHander.getFilesInfo(currrentUserDirTmp);
                clientHandler.sendCommand(requestDirOk(files, currrentUserDir));
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    public static void sendFile(RandomAccessFile aFile, ChannelHandlerContext ctx) {
        try {

            byte[] buffer = new byte[1024 * 512];

                while (true) {
                    long filePosition = aFile.getFilePointer();
                    int read = aFile.read(buffer);
                    if (read < buffer.length) {
                        byte[] finishBuffer = new byte[read];
                        if (finishBuffer.length >= 0) {
                            System.arraycopy(buffer, 0, finishBuffer, 0, finishBuffer.length);
                        }
                        Command response = Command.fileDataCommand(filePosition, finishBuffer);
                        ctx.writeAndFlush(response);
                        break;
                    } else {
                        Command response = Command.fileDataCommand(filePosition , buffer);
                        ctx.writeAndFlush(response);
                    }
                    buffer = new byte[1024 * 512];
                }
            } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    /**
     * Метод убирает знак ~ из начала пути на диске сервера
     * @param filePath
     * @return
     */
    public static String getUserPath(String filePath) {
        StringBuilder str = new StringBuilder();
        str.append(filePath);
        str.delete(0,1);//присылаемая строка с сервера вида ~/[текущий каталог], удаляем первый символ
        return str.toString();
    }
}
