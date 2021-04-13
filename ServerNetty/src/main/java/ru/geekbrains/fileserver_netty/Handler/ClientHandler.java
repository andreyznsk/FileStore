package ru.geekbrains.fileserver_netty.Handler;

import ClientServer.Command;
import ClientServer.FileInfo.FileInfo;
import ClientServer.commands.AuthCommandData;
import ClientServer.commands.AuthRegData;
import ClientServer.commands.FileSendCommandData;
import ClientServer.commands.RequestUserDir;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.SerializationUtils;
import ru.geekbrains.fileserver_netty.MyServer;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.List;

import static ClientServer.Command.*;

public class ClientHandler {

    private final MyServer myServer;
    private final ChannelHandlerContext ctx;

    private final int BUFFER_SIZE; // Стандартный размер буфера
    private String nickname;
    private String currentUserDir; //Текущая дирректория в которой находится клиент
    private String userPath;
    private RandomAccessFile aFile = null;

    /**
     * Конструктор создает экземпляр класса Обработчика клиентов
     * @param myServer
     * @param ctx
     */
    public ClientHandler(MyServer myServer, ChannelHandlerContext ctx) {
        this.myServer = myServer;
        this.ctx = ctx;
        BUFFER_SIZE = 1024*1024;
    }

    /**
     * Авторизация клиента
     * @throws IOException
     */
    public void authentication(Command command) throws IOException {

        if (command==null) return;
            switch (command.getType()) {
                case CREATE_NEW_USER: {
                AuthRegData data = (AuthRegData) command.getData();
                String login = data.getLogin();
                String password = data.getPassword();
                String nickname = data.getNickname();
                if (myServer.getAuthService().insertUser(login,password,nickname)==0) {
                    System.out.println("Ошибка создания пользователя");
                    sendCommand(errorCommand("Ошибка создания пользователя"));
                    return;
                } else sendCommand(confirmationCommand("Регистрация прошла успешно!"));
                return;
            }
                case UPDATE_USER: {
                AuthRegData data = (AuthRegData) command.getData();
                String login = data.getLogin();
                String password = data.getPassword();
                String nickname = data.getNickname();

                if (myServer.getAuthService().updateUser(login,password,nickname)==0) {
                    sendCommand(errorCommand("Логин или пароль некорркетны!"));
                    System.out.println("Логин или пароль некорркетны!");
                    return;
                } else {
                    sendCommand(confirmationCommand("Ник успешно изменен."));
                    System.out.println("Ник успешно изменен.");
                }
                    return;
            }
                case AUTH: {
                    AuthCommandData data = (AuthCommandData) command.getData();
                    String login = data.getLogin();
                    String password = data.getPassword();
                    String nickName = myServer.getAuthService().getNickByLoginPass(login, password);
                    System.out.println(nickName);
                    if (nickName == null) {
                        sendCommand(errorCommand("Некорректные логин или пароль!"));
                        System.out.println("Некорректные логин или пароль!");
                        return;
                    }
                    if (myServer.isNickBusy(nickName)) {
                        sendCommand(errorCommand("Пользователь уже подключен"));
                        return;
                    }
                    setNickname(nickName);
                    FileHander.isSrvDirectoryExist(userPath);//Проверка существования папка пользователя
                    List<FileInfo> files = FileHander.getFilesInfo(userPath);//Список файлов в папке пользователя
                    sendCommand(authOkCommand(this.nickname, files));
                    System.out.printf("Пользователь '%s' подключился!\n",this.nickname);
                    myServer.subscribe(this);
                    return;

                }
                case CLOSE_CONNECTION: {
                    myServer.unsubscribe(this);
                    sendCommand(closeConnection());
                    //client.close();
                    return;
                }
                default:
                    throw new IllegalArgumentException("Unknown command type: " + command.getType());
            }
    }


    /**
     * Прочитать команду от пользователя
     * @throws IOException
     */
    public void processClient(Command command)  {


        if (command == null) {
                return;
            }

            switch (command.getType()) {

                /*
                 * Команда на обновление списка файлов+папок сервера на стороне клиента
                 */
                case REQUEST_DIR: {
                    RequestUserDir data = (RequestUserDir) command.getData();
                    String tmp = FileHander.getUserPath(data.getRequestDir());//Метод убирает знак ~
                    currentUserDir = nickname + tmp;// добавляем каталог пользователя к запрашиваемому каталогу
                    if(FileHander.isSrvDirectory(currentUserDir)) {
                        List<FileInfo> files;
                        files = FileHander.getFilesInfo(currentUserDir); // формируем список файлов сервера для клиента
                        try {
                            sendCommand(requestDirOk(files, data.getRequestDir()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                }

                /*
                 * Команада запрос на соединение на прием файла от клиента на сервер
                 */
                case FILE_UPLOAD_REQEST:{
                    FileSendCommandData data = (FileSendCommandData) command.getData();
                    currentUserDir = data.getFilePath();
                    String tmp = FileHander.getUserPath(data.getFilePath());
                    String requestDir = nickname + tmp;// добавляем каталог пользователя к запрашиваемому каталогу
                    receiveFile(requestDir, data.getFileName(), data.getFileParts());// Поднимаем параллельный поток для приема файла
                    break;
                }
                /* Команада запрос на получение файла
                * */
                case FILE_DOWNLOAD_REQEST: {
                    FileSendCommandData data = (FileSendCommandData) command.getData();
                    currentUserDir = data.getFilePath();//Запомнить путь в который надо положить файл
                    String tmp = FileHander.getUserPath(data.getFilePath());//Метод убирает знак ~
                    String requestDir = nickname + tmp;// добавляем каталог пользователя к запрашиваемому каталогу
                    sendFile(requestDir, data.getFileName());// Поднимаем параллельный поток для отправки файла файла
                    break;
                }
                /*
                Если клиент разорвал соединение, закрыл окно или просто отключился
                 */
                case CLOSE_CONNECTION: {
                    try {
                        myServer.unsubscribe(this);
                        sendCommand(closeConnection());
                        //client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case END:
                    return;
                default:
                    throw new IllegalArgumentException("Unknown command type: " + command.getType());
            }
    }

    /**
     * Послать команду клиенту
     * @param command
     * @throws IOException
     */
   public void sendCommand(Command command) throws IOException {
        ctx.writeAndFlush(command);
    }

    /**
     * отдельный поток поднимается для загрузки фала от клиента на сервер
     * @param userPath - путь на сервере вида ./!ServerDisc/[Папка клиента]/.../имя файла
     * @param fileName - имя файла на сервере
     * @param fileParts - Количество частей файла которые необходимо прнять
     */
    private void receiveFile(String userPath, String fileName, long fileParts) {
        Thread thread = new Thread(() -> FileHander.receiveFile(userPath, fileName, currentUserDir, ctx, nickname, this, fileParts));
        thread.setDaemon(true);
        thread.start();
    }


    /**
     * Метод отправки файл клиенту
     * @param requestDir - запрашиваемая папка из которой нужно получить файл
     * @param fileName - имя файла
     */
    private void sendFile(String requestDir, String fileName) {
        try {
            Path p = FileHander.getPathByCurrentDir(requestDir);
            aFile = new RandomAccessFile(new File(p + "\\" + fileName), "r");
            long fileParts;
            //Вычислить сколько будет пересылаемых частей
            if (aFile.length() % BUFFER_SIZE != 0) fileParts = aFile.length() / BUFFER_SIZE + 1;
            else fileParts = aFile.length() / BUFFER_SIZE;
            System.out.println("Parts to send: " + fileParts);
            sendCommand(requestDownloadOk(fileParts));//Посылаем команды о готовности посылать файл,
            // клиент в этот момен переходит в режим чтения данных из буфера
            // клиент не закрывает цикл чтения пока не получит все части файла
            Thread thread = new Thread(() ->
                    FileHander.sendFile(aFile, ctx));//Отдельный поток читает данные из файла и посылает их клиенту в буфер
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    /**
     * Присваивает ник, и дирректорию клиента, устанавливает начальный путь
     * @param nickname
     */
    private void setNickname(String nickname) {
        this.nickname = nickname;
        this.userPath = nickname;
        this.currentUserDir = "~";
    }

    public String getNickname() {
        return nickname;
    }
}

