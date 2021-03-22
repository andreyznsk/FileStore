package server.Handler;

import ClientServer.Command;
import ClientServer.FileInfo;
import ClientServer.commands.AuthCommandData;
import ClientServer.commands.AuthRegData;
import ClientServer.commands.*;
import ClientServer.fileTransmitter.FileReceiver;
import ClientServer.fileTransmitter.FileSender;
import org.apache.commons.lang3.SerializationUtils;
import server.MyServer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.List;


import static ClientServer.Command.*;


public class ClientHandler {

    private final MyServer myServer;
    private final SocketChannel serverSocket;

    private String nickname;
    private String currrentUserDir;
    private String userPath;
    private String requestedDir;

    public ClientHandler(MyServer myServer, SocketChannel serverSocket) {
        this.myServer = myServer;
        this.serverSocket = serverSocket;
    }

    public void handle() {

        try {
                authentication(); // Если подключился новый клиент нужно пройти метод аутентификации

                readMessages(); // Если атентификация прошла, переходим в режим прослушивания команд от клиента
            } catch (IOException e) {
              e.printStackTrace();
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                  System.err.println("Failed to close connection!");
                }
            }
        }

    /**
     * отдельный поток поднимается для передачи фала от клиента на сервер
     * @param userPath - путь на сервере вида ./!ServerDisc/[Папка клиента]/.../имя файла
     * @param fileName - имя файла на сервере
     */
    private void fileReceiverThread(String userPath, String fileName) {

        Thread thread = new Thread(() -> {
                Path p = FileHander.getPathByCurrentDir(userPath);// Формируем путь к каталогу на сервере
                String userServerPath = p + "\\" + fileName;
                System.out.println("Fiile recive: " + userServerPath);
                FileReceiver nioServer = new FileReceiver();
                SocketChannel socketChannel = nioServer.createServerSocketChannel();
                nioServer.readFileFromSocket(socketChannel,userServerPath);
                List<FileInfo> files;
                System.out.println("Путь от которого формируем файлы: " + currrentUserDir);
                StringBuilder str = new StringBuilder();
                str.append(currrentUserDir);
                str.delete(0,1);//присылаемая строка с сервера вида ~/[текущий каталог], удаляем первый символ
                String currrentUserDirTmp = nickname + str;// добавляем каталог пользователя к запрашиваемому каталогу
                System.out.println("Искомая дирректория: " + currrentUserDirTmp);
                files = FileHander.getFilesInfo(currrentUserDirTmp);

            try {
                sendCommand(requestDirOk(files, currrentUserDir));
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        thread.setDaemon(true);
        thread.start();

    }

    private void fileSenderThread(String requestDir, String fileName) {

        Thread thread = new Thread(() -> {
            Path p = FileHander.getPathByCurrentDir(requestDir);
            String userServerPath = p + "\\" + fileName;
            System.out.println("Fiile recive: " + userServerPath);
            FileSender nioClient = new FileSender();
            SocketChannel socketChannel = nioClient.createChannel();
            nioClient.sendFile(socketChannel, userServerPath);

            try {
                sendCommand(requestReciveOk());
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        thread.setDaemon(true);
        thread.start();
    }


    private void authentication() throws IOException {
        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }
            switch (command.getType()) {
                case CREATE_NEW_USER: {
                AuthRegData data = (AuthRegData) command.getData();
                String login = data.getLogin();
                String password = data.getPassword();
                String nickname = data.getNickname();
                if (myServer.getAuthService().insertUser(login,password,nickname)==0) {
                    System.out.println("Ошибка создания пользователя");
                    sendCommand(errorCommand("Ошибка создания пользователя"));
                    continue;
                } else sendCommand(confirmationCommand("Регистрация прошла успешно!"));
                break;
            }
                case UPDATE_USER: {
                AuthRegData data = (AuthRegData) command.getData();
                String login = data.getLogin();
                String password = data.getPassword();
                String nickname = data.getNickname();

                if (myServer.getAuthService().updateUser(login,password,nickname)==0) {

                    sendCommand(errorCommand("Логин или пароль некорркетны!"));
                    System.out.println("Логин или пароль некорркетны!");
                    continue;
                } else {
                    sendCommand(confirmationCommand("Ник успешно изменен."));
                    System.out.println("Ник успешно изменен.");
                }
                break;
            }
                case AUTH: {
                    AuthCommandData data = (AuthCommandData) command.getData();
                    String login = data.getLogin();

                    String password = data.getPassword();
                    String[] nickAndPath = myServer.getAuthService().getNickByLoginPass(login, password);

                    if (nickAndPath[0] == null) {
                        sendCommand(errorCommand("Некорректные логин или пароль!"));
                        System.out.println("Некорректные логин или пароль!");
                        continue;
                    }
                    setNickname(nickAndPath[0]);
                    FileHander.isSrvDirectoryExist(nickAndPath[1]);
                    List<FileInfo> files = FileHander.getFilesInfo(nickAndPath[1]);
                    sendCommand(authOkCommand(getNickname(), files));
                    System.out.println("Пользователь '%s' подключился!");
                    myServer.subscribe(this);
                    return;
                }
                default:
                    throw new IllegalArgumentException("Unknown command type: " + command.getType());
            }
        }
    }

    /**
     * Бесконечный цикл прослушивания команд от клиента на сервер
     * @throws IOException
     */
    private void readMessages() throws IOException {
        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            switch (command.getType()) {

                /**
                 * Команда на обновление списка файлов+папок сервера на стороне клиента
                 */
                case REQUEST_DIR: {
                    RequestUserDir data = (RequestUserDir) command.getData();
                    StringBuilder str = new StringBuilder();
                    requestedDir = data.getRequestDir();
                    str.append(requestedDir);
                    str.delete(0,1);//присылаемая строка с сервера вида ~/[текущий каталог], удаляем первый символ
                    currrentUserDir = nickname + str;// добавляем каталог пользователя к запрашиваемому каталогу
                    System.out.println("Искомая дирректория: " + currrentUserDir);
                    if(FileHander.isSrvDirectory(currrentUserDir)) {
                        List<FileInfo> files;
                        files = FileHander.getFilesInfo(currrentUserDir); // формируем список файлов сервера для клиента
                        sendCommand(requestDirOk(files, requestedDir));}

                    break;
                }

                /**
                 * Команада щапрос на соединение на прием файла от клиента на сервер
                 */
                case FILE_SEND_REQEST:{
                    FileSendCommandData data = (FileSendCommandData) command.getData();
                    StringBuilder str = new StringBuilder();
                    currrentUserDir = data.getFilePath();
                    //if(currrentUserDir.equals("~")) currrentUserDir = userPath;
                    str.append(data.getFilePath());
                    str.delete(0,1);//присылаемая строка с сервера вида ~/[текущий каталог], удаляем первый символ
                    String requestDir = nickname + str;// добавляем каталог пользователя к запрашиваемому каталогу
                    fileReceiverThread(requestDir, data.getFileName());// Поднимаем параллельный поток для приема файла
                    sendCommand(requestTransmiterOk());
                    break;
                }
                case FILE_RECIVE_REQEST: {
                    FileSendCommandData data = (FileSendCommandData) command.getData();
                    StringBuilder str = new StringBuilder();
                    currrentUserDir = data.getFilePath();
                    //if(currrentUserDir.equals("~")) currrentUserDir = userPath;
                    str.append(data.getFilePath());
                    str.delete(0,1);//присылаемая строка с сервера вида ~/[текущий каталог], удаляем первый символ
                    String requestDir = nickname + str;// добавляем каталог пользователя к запрашиваемому каталогу
                    fileSenderThread(requestDir, data.getFileName());// Поднимаем параллельный поток для приема файла

                    break;
                }


                case END:
                    return;
                default:
                    throw new IllegalArgumentException("Unknown command type: " + command.getType());

            }
        }
    }

    public String getNickname() {
        return nickname;
    }

    public void sendCommand(Command command) throws IOException {
        byte[] data = SerializationUtils.serialize(command);
        serverSocket.write(ByteBuffer.wrap(data));
    }

    private Command readCommand() throws IOException {
        Command command = null;
            byte[] data = new byte[1024];
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int r = serverSocket.read(byteBuffer);
            if(r!=0) {
                //System.out.println(r);
                while (r != 0) {
                    byteBuffer.flip();
                    int i = 0;
                    while (byteBuffer.hasRemaining()) {
                        data[i] = byteBuffer.get();
                        //System.out.println(data[i]);
                        i++;
                    }
                    byteBuffer.clear();
                    r = serverSocket.read(byteBuffer);
                }
                //System.out.println(data);
                command = SerializationUtils.deserialize(data);

            }
            return command;
    }



    private void closeConnection() throws IOException {
        myServer.unsubscribe(this);
        serverSocket.close();

    }

    private void setNickname(String nickname) {
        this.nickname = nickname;
        this.userPath = nickname;
        this.currrentUserDir = "~";
    }
}

