package ru.geekbrains.fileserver_netty.models;


import ClientServer.Command;
import ClientServer.CommandType;
import ClientServer.FileInfo.FileInfo;
import ClientServer.commands.*;
import javafx.application.Platform;
import org.apache.commons.lang3.SerializationUtils;
import ru.geekbrains.fileserver_netty.ClientChatNetty;
import ru.geekbrains.fileserver_netty.ViewController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Stack;

import static ClientServer.Command.*;
//import static ClientServer.Command.updateUserPath;

public class Network {
        private static final int BUFFER_SIZE = 1024;
        private static final String SERVER_ADDRESS = "localhost";
        private static final int SERVER_PORT = 9000;

        private SocketChannel clientSocket;

        private Stack<String> currentUserDir;

        private final String host;
        private final int port;

        private RandomAccessFile aFile = null;
        private ClientChatNetty clientChat;
        private String nickname;
        private String remoutePath;
        private List<FileInfo> files;
        private String userFile;

    public Network() {
            this(SERVER_ADDRESS, SERVER_PORT);
        }

        public Network(String host, int port) {
            this.host = host;
            this.port = port;
            currentUserDir = new Stack<>();
        }

        public Network(ClientChatNetty clientChat) {
            this();
            this.clientChat = clientChat;
        }

        public boolean connect() {
            try {
                clientSocket = SocketChannel.open(new InetSocketAddress(host, port));
                clientSocket.configureBlocking(false);
                return true;
            } catch (IOException e) {
                System.err.println("Соединение не было установлено!");
                e.printStackTrace();
                return false;
            }
        }

        private void sendCommand(Command command) throws IOException {
            byte[] data = SerializationUtils.serialize(command);
            clientSocket.write(ByteBuffer.wrap(data));
            clientSocket.write(ByteBuffer.wrap(System.lineSeparator().getBytes()));
        }

        public void waitMessages(ViewController viewController) {
            Thread thread = new Thread(() -> {
                try {
                    while (true) {
                        Command command = readCommand();
                        if (command == null) {
                            continue;
                        }

                        if (clientChat.getState() == ClientChatState.AUTHENTICATION) {

                            processAuthResult(command);

                        } else {
                            processMessage(viewController, command);
                        }
                    }
                } catch (IOException e) {
                   //close();
                    System.out.println("Соединение было потеряно!");
                    //Platform.exit();
                }
            });
            thread.setDaemon(true);
            thread.start();
        }

        private void processMessage(ViewController viewController, Command command) {
            switch (command.getType()) {
                /**
                 * ответ от сервера на запрос обновления каталога
                 */
                case REQUEST_DIR_OK:{
                    RequestDirOkData data = (RequestDirOkData) command.getData();
                    if (!currentUserDir.peek().equals(data.getNextPath())) currentUserDir.add(data.getNextPath()); //Данная проверка нужна, если происходит переход
                    // на уровень выше, то текущий катлог на клиенте совпадает с каталогом который прислал сервер, его не записываем
                    String updatedCurrentPath = currentUserDir.peek();
                    Platform.runLater(() -> {
                        viewController.updateRemoteList(nickname,updatedCurrentPath, data.getFiles());
                    });
                    break;
                }

                case REQUEST_TRANSMITTER_OK: {
                    System.out.println("Await for transmitt");
                    sendFileToServer();
                    break;
                }

                case REQUEST_DOWNLOAD_OK: {
                    FileSendCommandData data = (FileSendCommandData) command.getData();
                    receiveFileFromServer(data.getFileParts());
                    Platform.runLater(viewController::updateClientDir);
                    break;
                }

                case CLOSE_CONNECTION: {
                    System.out.println("Try to close connection!!!!");
                    if (clientSocket != null && clientSocket.isConnected()) {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Platform.exit();
                    break;
                }

                    case ERROR: {
                    ErrorCommandData data = (ErrorCommandData) command.getData();
                    Platform.runLater(() -> {
                        ClientChatNetty.showNetworkError(data.getErrorMessage(), "Server error", null);
                    });
                    break;
                }

                default:
                    throw new IllegalArgumentException("Unknown command type: " + command.getType());
            }
        }

    private void receiveFileFromServer(long fileParts) {
        System.out.println("File receive: " + userFile);
            RandomAccessFile aFile = null;
            try {
                aFile = new RandomAccessFile(userFile, "rw");
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                FileChannel fileChannel = aFile.getChannel();
                Command command = readCommand();

                while (command.getType() == CommandType.FILE_DATA) {
                    if (command == null) {
                        continue;
                    }
                    FileDataCommand data = (FileDataCommand) command.getData();
                        fileChannel.write(ByteBuffer.wrap(data.getData()));
                        buffer.clear();
                        System.out.println("File received parts: " + fileParts--);

                }

                fileChannel.close();
                System.out.println("End of file reached..Closing channel");

            } catch (IOException e) {
                if (fileParts != 0) System.err.println("Fail receive failed!!!");
                e.printStackTrace();
                //clientSocket.close();
            }

    }


    private void processAuthResult(Command command) {
            switch (command.getType()) {
                case AUTH_OK: {
                    AuthOkCommandData data = (AuthOkCommandData) command.getData();
                    nickname = data.getUsername();
                    remoutePath = data.getPath();
                    files = data.getFiles();
                    currentUserDir.add("~");
                    Platform.runLater(() -> {
                        ClientChatNetty.showNetworkConfirmation("Регистрация прошла успешно", "Успешно", null);

                        clientChat.activeChatDialog(nickname, remoutePath,files);
                    });
                    break;
                }

                case ERROR: {
                    ErrorCommandData data = (ErrorCommandData) command.getData();
                    Platform.runLater(() -> {
                        ClientChatNetty.showNetworkError(data.getErrorMessage(), "Auth error", null);
                    });
                    break;
                }

                case CONFIRMATION: {
                    Platform.runLater(() -> {
                        ClientChatNetty.showNetworkConfirmation("Регистрация прошла успешно", "Успешно", null);
                    });
                    break;
                }

                case CLOSE_CONNECTION: {
                    System.out.println("Try to close connection!!!!");
                    if (clientSocket != null && clientSocket.isConnected()) {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Platform.exit();
                    break;
                }

                default:
                    throw new IllegalArgumentException("Uknown command type: " + command.getType());
            }
        }

        public void close() {
            //TODO Почему при закрытии удаленного сокета сервер съедает всю оперативную память и idea перестает запускаться?
            try {
                System.out.println("Connection close!!!");
                sendCommand(closeConnection());
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Platform.exit();

        }

        private Command readCommand() throws IOException {
            Command command = null;
            byte[] data = new byte[BUFFER_SIZE];
            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            int r = clientSocket.read(byteBuffer);
            if(r!=0) {
                while (r > 0) {
                    byteBuffer.flip();
                    int i = 0;
                    while (byteBuffer.hasRemaining()) {
                        data[i] = byteBuffer.get();
                        i++;
                    }
                    byteBuffer.clear();
                    r = clientSocket.read(byteBuffer);
                }
             command = SerializationUtils.deserialize(data);
            }

            return command;
        }

        public void sendAuthMessage(String login, String password) throws IOException {
            sendCommand(authCommand(login, password));
        }

    public void sendNewUserCommand(String login, String password, String nickname) throws IOException {
            sendCommand(regNewUserCommand(login, password, nickname));

    }

    public void sendUpdateUserCommand(String login, String password, String nickname) throws IOException {
        sendCommand(regUpdateUserCommand(login, password, nickname));
    }

    /**
     * Перегруженный метод переход в запрашиваемый каталог на сервере
      * @param requestPath - запрашиваемый каталог
     * @throws IOException
     */
    public void sendUpdateRemotePath(String requestPath) throws IOException{
        System.out.println("Текущий каталог: " + currentUserDir);
        String tmp = currentUserDir.peek() + "/" + requestPath; // отправить на сервер текущий каталог + запрашиваемый каталог
        System.out.println("Посылаемая строка на сервер: " + tmp);
            sendCommand(updateUserPath(tmp));
    }

    /**
     * Перегруженный метод перехода на уроень выше на стороне сервера
     * отправляет запрос на обновеление списка файлов и папок на уровне выше
     * @throws IOException
     */
    public void sendUpdateRemotePath() throws IOException{
        System.out.println(currentUserDir);//техническая информация
            if(currentUserDir.peek().equals("~")) return; //Проверка если текущая директория корневая, то выйти
            currentUserDir.pop();// Удаляем текущий каталог
        sendCommand(updateUserPath(currentUserDir.peek()));// посылаем на сервер запрос списка предыдущего каталога

    }

    /**
     * Метод запроса на соединение для пересылки файла от клиента на сервер
     * @param  userFile - полный путь к файлу на стороне клиента
     * @param  fileName - Название файла
     */
    public void requestSendFile(String userFile, String fileName) {
        System.out.println("Пересылемый файл " + fileName);
        this.userFile = userFile;
        try {
       this.aFile = null;
        aFile = new RandomAccessFile( new File(userFile), "r");
        long fileParts;
        if(aFile.length() % BUFFER_SIZE != 0) fileParts = aFile.length() / BUFFER_SIZE + 1;
        else fileParts = aFile.length() / BUFFER_SIZE;
            System.out.println("Parts to send: " + fileParts);
            sendCommand(fileSendCommand(currentUserDir.peek(),fileName,fileParts));
         } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Если сервер готов к приему файла начинаем слать файл
     */
    public void sendFileToServer()
    {
        try {
            FileChannel inChannel = aFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            int i = 0;
            while (inChannel.read(buffer) > 0) {
                System.out.println("Part: " + i);
                try {
                    Thread.sleep(500);//Иммитация медленного клиента
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                buffer.flip();
                clientSocket.write(buffer);
                buffer.clear();
                i++;
            }
            System.out.println("End of file reached..");
            //sendCommand(Command.endOfFile());
            aFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Запрос на прием файла с сервера
     * @param targetPath - в какой файл положить файл на клиенте
     * @param srcFileName
     */
    public void requestReceiveFile(String targetPath, String srcFileName) {
        System.out.println("Пересылемый файл " + srcFileName);
        this.userFile = targetPath;
        try {
            sendCommand(fileReceiveCommand(currentUserDir.peek(),srcFileName, 0l));
        } catch (Exception e) {
            e.printStackTrace();

        }

    }
}
