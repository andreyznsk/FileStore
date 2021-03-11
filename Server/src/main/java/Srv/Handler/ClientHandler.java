package Srv.Handler;

import ClaintServer.Command;
import ClaintServer.CommandType;
import ClaintServer.commands.AuthCommandData;
import ClaintServer.commands.AuthRegData;

import ClaintServer.commands.PrivateMessageCommandData;
import ClaintServer.commands.PublicMessageCommandData;
import Srv.MyServer;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.FileInputStream;
import java.io.IOException;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


import static ClaintServer.Command.*;


public class ClientHandler {

    private final MyServer myServer;
    private final SocketChannel clientSocket;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(256);

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private String nickname;

    public ClientHandler(MyServer myServer, SocketChannel clientSocket) {
        this.myServer = myServer;
        this.clientSocket = clientSocket;
        LogManager manager = LogManager.getLogManager();
        try {
            manager.readConfiguration(new FileInputStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle() throws IOException {


        try {
            authentication();
            // readMessages();
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



    private void authentication() throws IOException {
        readCommand();
       /* while (true) {
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
                    continue;
                } else {
                    sendCommand(confirmationCommand("Ник успешно изменен."));
                }
            }


            if (command.getType() == CommandType.AUTH) {
                AuthCommandData data = (AuthCommandData) command.getData();
                String login = data.getLogin();
                String password = data.getPassword();
                String nickname = myServer.getAuthService().getNickByLoginPass(login, password);
                if (nickname == null) {
                    sendCommand(errorCommand("Некорректные логин или пароль!"));
                    continue;
                }

                if (myServer.isNickBusy(nickname)) {
                    sendCommand(errorCommand("Такой пользователь уже вошел в чат!"));
                    continue;
                }

                sendCommand(authOkCommand(nickname));
                setNickname(nickname);
                myServer.subscribe(this);
                return;
            }
        }*/
    }

    public void sendCommand(Command command) throws IOException {
        out.writeObject(command);
    }

    private Command readCommand() throws IOException {
        Command command = null;

            clientSocket.read(byteBuffer);
        //SerializationUtils.deserialize(byteBuffer.array());
        System.out.println(byteBuffer.array());
        byteBuffer.clear();
        return command;
    }

    private void readMessages() throws IOException {
        readCommand();
        /*while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            switch (command.getType()) {
                case PRIVATE_MESSAGE: {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                    String receiver = data.getReceiver();
                    String message = data.getMessage();
                    myServer.sendPrivateMessage(this, receiver, message);
                    break;
                }
                case PUBLIC_MESSAGE: {
                    PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
                    String message = data.getMessage();
                   // myServer.broadcastMessage(message, this);
                    break;
                }
                case END:
                    return;
                default:
                    throw new IllegalArgumentException("Unknown command type: " + command.getType());

            }
        }*/
    }

    private void closeConnection() throws IOException {
        myServer.unsubscribe(this);
        clientSocket.close();
    }


    public void sendMessage(String message) throws IOException {
        sendCommand(Command.messageInfoCommand(message));
    }

    public void sendMessage(String sender, String message) throws IOException {
        sendCommand(clientMessageCommand(sender, message));
    }

    public String getNickname() {
        return nickname;
    }

    private void setNickname(String nickname) {
        this.nickname = nickname;
    }
}