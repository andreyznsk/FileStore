package server.Handler;

import commands.AuthCommandData;
import commands.AuthRegData;
import commands.PrivateMessageCommandData;
import commands.PublicMessageCommandData;
import org.apache.commons.lang3.SerializationUtils;
import server.MyServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;



public class ClientHandler {
    private static final Logger logger = Logger.getLogger(MyServer.class.getName());
    private final MyServer myServer;
    private final SocketChannel serverSocket;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private String nickname;

    public ClientHandler(MyServer myServer, SocketChannel serverSocket) {
        this.myServer = myServer;
        this.serverSocket = serverSocket;
        LogManager manager = LogManager.getLogManager();
        try {
            manager.readConfiguration(new FileInputStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle() throws IOException {


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
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(nickname==null) {//если логин не получен закрыть соединение
                    try {
                        sendCommand(closeByTimer());//Посылаем клиенту команду о разрыве соединения по таймеру
                       // logger.log(Level.SEVERE, "Закрыаем соединение");
                        System.out.println("Закрыаем соединение");
                        closeConnection();

                    } catch (IOException e) {
                        //logger.log(Level.WARNING, "Не смогли прервать подключение",e);
                       System.err.println("Не смогли прервать подключение");

                    }
                }
            }
        };

        Timer timer = new Timer(true);

        timer.schedule(timerTask, 120000);//Запуск отдельного потока, который проверят ести ли логин
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
                    logger.log(Level.SEVERE, "Такой ник уже есть!!");
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
                    logger.log(Level.SEVERE, "Логин или пароль некорркетны!");
                    continue;
                } else {
                    sendCommand(confirmationCommand("Ник успешно изменен."));
                    logger.log(Level.SEVERE, "Ник успешно изменен.");
                }
            }


            if (command.getType() == CommandType.AUTH) {
                AuthCommandData data = (AuthCommandData) command.getData();
                String login = data.getLogin();
                String password = data.getPassword();
                String nickname = myServer.getAuthService().getNickByLoginPass(login, password);
                if (nickname == null) {
                    sendCommand(errorCommand("Некорректные логин или пароль!"));
                    logger.log(Level.SEVERE, "Некорректные логин или пароль!");
                    continue;
                }

                if (myServer.isNickBusy(nickname)) {
                    sendCommand(errorCommand("Такой пользователь уже вошел в чат!"));
                    logger.log(Level.SEVERE, "Такой пользователь уже вошел в чат!");
                    continue;
                }

                sendCommand(authOkCommand(nickname));
                setNickname(nickname);
                logger.log(Level.SEVERE, String.format("Пользователь '%s' зашел в чат!", nickname));
                myServer.broadcastMessage(String.format("Пользователь '%s' зашел в чат!", nickname), null);
                myServer.subscribe(this);
                return;
            }
        }
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
            //System.out.println(Arrays.toString(data));
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
                    myServer.broadcastMessage(message, this);
                    break;
                }
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

