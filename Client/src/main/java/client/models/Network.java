package client.models;


import ClientServer.Command;
import ClientServer.FileInfo;
import ClientServer.FileTransmitter.FileReader;
import ClientServer.FileTransmitter.FileSender;
import ClientServer.commands.*;
import client.ClientChat;
import client.ViewController;
import javafx.application.Platform;
import org.apache.commons.lang3.SerializationUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Stack;

import static ClientServer.Command.*;
import static ClientServer.Command.updateUserPath;

public class Network {

        private static final String SERVER_ADDRESS = "localhost";
        private static final int SERVER_PORT = 9000;

        private SocketChannel clientSocket;

        private Stack<String> currentUserDir;

        private String host;
        private int port;


        private ClientChat clientChat;
        private String nickname;
        private String remoutePath;
        private List<FileInfo> files;

        public Network() {
            this(SERVER_ADDRESS, SERVER_PORT);
        }

        public Network(String host, int port) {
            this.host = host;
            this.port = port;
            currentUserDir = new Stack<>();
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
                case REQUEST_DIR_OK:{
                    RequestDirOkData data = (RequestDirOkData) command.getData();
                    if (!currentUserDir.peek().equals(data.getNextPath())) currentUserDir.add(data.getNextPath());
                    String updatedCurrentPath = currentUserDir.peek();
                    Platform.runLater(() -> {
                        viewController.updateRemoteList(nickname,updatedCurrentPath, data.getFiles());
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

                default:
                    throw new IllegalArgumentException("Uknown command type: " + command.getType());
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
                        ClientChat.showNetworkConfirmation("Регистрация прошла успешно", "Успешно", null);

                        clientChat.activeChatDialog(nickname, remoutePath,files);
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
                while (r != 0) {
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

    public void sendUpdateRemotePath(String requestPath) throws IOException{
        System.out.println("Текущий каталог"+currentUserDir);
      /*  if (currentUserDir.peek().equals("~")) {
            sendCommand(updateUserPath("/" + requestPath));
            System.out.println(requestPath);
            return;
        }*/
            String tmp = currentUserDir.peek() + "/" + requestPath;

        System.out.println("Посылаемая строка на сервер: " + tmp);
            sendCommand(updateUserPath(tmp));
    }

    public void sendUpdateRemotePath() throws IOException{
        System.out.println(currentUserDir);
            if(currentUserDir.peek().equals("~")) return;
            currentUserDir.pop();
        sendCommand(updateUserPath(currentUserDir.peek()));

    }

    public void sendFileToServer(String fileName) {
        System.out.println("Пересылемый файл " + fileName);

        try {
            System.out.println("File transfer: " + fileName);
            FileInputStream fis = new FileInputStream(fileName);
            FileChannel inChannel = fis.getChannel();
            sendCommand(fileSendCommand(currentUserDir.peek() + "/" + fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
