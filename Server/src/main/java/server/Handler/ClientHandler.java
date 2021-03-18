package server.Handler;

import ClientServer.Command;
import ClientServer.FileInfo;
import ClientServer.FileTransmitter.FileReceiver;
import ClientServer.FileTransmitter.FileWriter;
import ClientServer.commands.AuthCommandData;
import ClientServer.commands.AuthRegData;
import ClientServer.commands.*;
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

    private String userPath;

    public ClientHandler(MyServer myServer, SocketChannel serverSocket) {
        this.myServer = myServer;
        this.serverSocket = serverSocket;
    }

    public void handle() {

        try {
                authentication();

                readMessages();
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

    private void fileResiverThread(String fileName) {
        Thread thread = new Thread(() -> {
            try {
                FileOutputStream fos = new FileOutputStream("!ServerDisc/" + fileName);
                FileChannel outChannel = fos.getChannel();
                System.out.println("Как переслать файл");
                System.out.println("Fiile recive: " + "!ServerDisc/" + fileName );
            } catch (IOException e) {

                System.out.println("Поток отправки файлов потерян");
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
                    System.out.println("Такой ник уже есть!!");
                    sendCommand(errorCommand("Такой ник уже есть!!"));
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
    private void readMessages() throws IOException {
        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            switch (command.getType()) {


                case REQUEST_DIR: {
                    RequestUserDir data = (RequestUserDir) command.getData();
                    StringBuilder str = new StringBuilder();
                    str.append(data.getRequestDir());
                    str.delete(0,1);
                    String requestDir = userPath + str;
                    System.out.println("Искомая дирректория: " + requestDir);
                    if(FileHander.isSrvDirectory(requestDir)) {
                        List<FileInfo> files;
                        files = FileHander.getFilesInfo(requestDir);
                        sendCommand(requestDirOk(files, data.getRequestDir()));}

                    break;
                }

                case FILE_SEND_REQEST:{
                    FileSendCommandData data = (FileSendCommandData) command.getData();
                    StringBuilder str = new StringBuilder();
                    str.append(data.getFileName());
                    str.delete(0,1);
                    String requestDir = userPath + str;
                    System.out.println(requestDir);
                    Path p = FileHander.getPathByCurrentDir(requestDir);
                    fileResiverThread(data.getFileName());
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
                System.out.println(data);
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
    }
}

