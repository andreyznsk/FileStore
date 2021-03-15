package server.Handler;

import ClientServer.Command;
import ClientServer.CommandType;
import ClientServer.commands.AuthCommandData;
import ClientServer.commands.AuthRegData;
import org.apache.commons.lang3.SerializationUtils;
import server.MyServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import static ClientServer.Command.*;


public class ClientHandler {

    private final MyServer myServer;
    private final SocketChannel serverSocket;

    private String nickname;
    private Path userPath;

    public ClientHandler(MyServer myServer, SocketChannel serverSocket) {
        this.myServer = myServer;
        this.serverSocket = serverSocket;
    }

    public void handle() {


        ExecutorService executorService = Executors.newCachedThreadPool();

        executorService.execute(() -> {
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
        });
    }

    private void authentication() throws IOException {

        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            if(command.getType() == CommandType.CREATE_NEW_USER) {
                AuthRegData data = (AuthRegData) command.getData();
                String login = data.getLogin();
                String password = data.getPassword();
                String nickname = data.getNickname();
                if (myServer.getAuthService().insertUser(login,password,nickname)==0) {
                    System.out.println("Такой ник уже есть!!");
                    sendCommand(errorCommand("Такой ник уже есть!!"));
                    continue;
                } else sendCommand(confirmationCommand("Регистрация прошла успешно!"));
            }

            if(command.getType() == CommandType.UPDATE_USER) {
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
            }


            if (command.getType() == CommandType.AUTH) {
                AuthCommandData data = (AuthCommandData) command.getData();
                String login = data.getLogin();

                String password = data.getPassword();
                String[] nickAndPath  = myServer.getAuthService().getNickByLoginPass(login, password);

                if (nickAndPath[0] == null) {
                    sendCommand(errorCommand("Некорректные логин или пароль!"));
                    System.out.println("Некорректные логин или пароль!");
                    continue;
                }
                setNickname(nickAndPath[0]);
                setUserPath(nickAndPath[1]);
                sendCommand(authOkCommand(getNickname(),nickAndPath[1]));

                System.out.println("Пользователь '%s' подключился!");

                myServer.subscribe(this);
                return;
            }
        }
    }

    private void setUserPath(String path) {
        this.userPath = Paths.get(".",path);
    }

    public Path getUserPath() {
        return userPath;
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
                command = SerializationUtils.deserialize(data);
            }
            return command;
    }

    private void readMessages() throws IOException {
        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            switch (command.getType()) {
               case END:
                    return;
                default:
                    throw new IllegalArgumentException("Unknown command type: " + command.getType());

            }
        }
    }

    private void closeConnection() throws IOException {
        myServer.unsubscribe(this);
        serverSocket.close();

    }

    private void setNickname(String nickname) {
        this.nickname = nickname;
    }
}

