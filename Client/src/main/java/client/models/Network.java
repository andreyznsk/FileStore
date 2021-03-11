package client.models;


import ClaintServer.Command;
import ClaintServer.commands.*;
import client.ClientChat;
import client.ViewController;
import javafx.application.Platform;
import org.apache.commons.lang3.SerializationUtils;


import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;


import static ClaintServer.Command.*;

public class Network {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9000;
    private SocketChannel channel;

    private String host;
    private int port;

    private ClientChat clientChat;
    private String nickname;


    public Network() {
        this(SERVER_ADDRESS, SERVER_PORT);
    }

    public Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Network(ClientChat clientChat) {
        this();
        this.clientChat = clientChat;
    }

    public boolean connect() {
        try {
            channel = SocketChannel.open(new InetSocketAddress("localhost", 9000));

            return true;
        } catch (IOException e) {
            System.err.println("Соединение не было установлено!");
            e.printStackTrace();
            return false;
        }
    }

    public void sendPrivateMessage(String receiver, String message) throws IOException {
        sendCommand(privateMessageCommand(receiver, message));
    }

    public void sendMessage(String message) throws IOException {
        sendCommand(publicMessageCommand(nickname, message));
    }

    private void sendCommand(Command command) throws IOException {
       byte[] data = SerializationUtils.serialize(command);
        System.out.println(data);
        channel.write(ByteBuffer.wrap(data));
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
                close();
                System.out.println("Соединение было потеряно!");
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void processMessage(ViewController viewController, Command command) {
        switch (command.getType()) {
            case INFO_MESSAGE: {
                MessageInfoCommandData data = (MessageInfoCommandData) command.getData();
                Platform.runLater(() -> {
                    viewController.appendMessage(data.getMessage());
                });
                break;
            }
            case CLIENT_MESSAGE: {
                ClientMessageCommandData data = (ClientMessageCommandData) command.getData();
                String sender = data.getSender();
                String message = data.getMessage();
                Platform.runLater(() -> {
                    viewController.appendMessage(String.format("%s: %s", sender, message));
                });
                break;
            }
            case ERROR: {
                ErrorCommandData data = (ErrorCommandData) command.getData();
                Platform.runLater(() -> {
                    ClientChat.showNetworkError(data.getErrorMessage(), "Server error", null);
                });
                break;
            }
            case UPDATE_USER_LIST: {
                UpdateUsersListCommandData data = (UpdateUsersListCommandData) command.getData();
                Platform.runLater(() -> {
                    clientChat.updateUsers(data.getUsers());
                });
                break;
            }
            default:
                throw new IllegalArgumentException("Uknown command type: " + command.getType());
        }
    }

    private void processAuthResult(Command command) {
        switch (command.getType()) {
            case AUTH_OK: {
                AuthOkCommandData data = (AuthOkCommandData) command.getData();
                nickname = data.getUsername();
                Platform.runLater(() -> {
                    ClientChat.showNetworkConfirmation("Регистрация прошла успешно", "Успешно", null);
                    clientChat.activeChatDialog(nickname);
                });
                break;
            }
            case CLOSE_BY_TIMER:{//Дополнительная служебная команда об ошибке по таймеру
                Platform.runLater(() -> {
                    ClientChat.showNetworkError("Соединение разорвано по таймеру", "Auth error", null);});


                break;
            }


            case ERROR: {
                ErrorCommandData data = (ErrorCommandData) command.getData();
                Platform.runLater(() -> {
                    ClientChat.showNetworkError(data.getErrorMessage(), "Auth error", null);
                });
                break;
            }

            case CONFIRMATION:
                //ErrorCommandData data = (ErrorCommandData) command.getData();
                Platform.runLater(() -> {
                    ClientChat.showNetworkConfirmation("Регистрация прошла успешно", "Успешно", null);
                });
                break;

            case UPDATE_USER_LIST: {
                UpdateUsersListCommandData data = (UpdateUsersListCommandData) command.getData();
                Platform.runLater(() -> {
                    clientChat.updateUsers(data.getUsers());
                });
                break;
            }
            default:
                throw new IllegalArgumentException("Uknown command type: " + command.getType());
        }
    }

    public void close() {

        try {
            if (channel != null && channel.isConnected()) {
                channel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Command readCommand() throws IOException {
     /*   Command command = null;
        try {
            command = (Command) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to read Command class");
            e.printStackTrace();
        }

        return command;*/
        return null;
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

}