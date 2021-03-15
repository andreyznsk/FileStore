package client.models;


import ClientServer.Command;
import ClientServer.commands.*;
import client.ClientChat;
import client.ViewController;
import javafx.application.Platform;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ClientServer.Command.*;

public class Network {

        private static final String SERVER_ADDRESS = "localhost";
        private static final int SERVER_PORT = 9000;
        private SocketChannel clientSocket;

        private String host;
        private int port;


        private ClientChat clientChat;
        private String nickname;
        private String remoutePath;

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

                case ERROR: {
                    ErrorCommandData data = (ErrorCommandData) command.getData();
                    Platform.runLater(() -> {
                        ClientChat.showNetworkError(data.getErrorMessage(), "Server error", null);
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
                    System.out.println("remote path = " + data.getPath());
                    remoutePath = data.getPath();
                    Platform.runLater(() -> {
                        ClientChat.showNetworkConfirmation("Регистрация прошла успешно", "Успешно", null);
                        clientChat.activeChatDialog(nickname, remoutePath);
                    });
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

                    Platform.runLater(() -> {
                        ClientChat.showNetworkConfirmation("Регистрация прошла успешно", "Успешно", null);
                    });
                    break;

                default:
                    throw new IllegalArgumentException("Uknown command type: " + command.getType());
            }
        }

        public void close() {
            //TODO Почему при закрытии удаленного сокета сервер съедает всю оперативную память и idea перестает запускаться?
           /* try {
                if (clientSocket != null && clientSocket.isConnected()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            Platform.exit();

        }

        private Command readCommand() throws IOException {
            Command command = null;
            byte[] data = new byte[1024];
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int r = clientSocket.read(byteBuffer);
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

}
