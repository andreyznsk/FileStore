package server;

import ClientServer.Command;
import server.Handler.ClientHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;


public class MyServer {

    private final List<ClientHandler> clients = new ArrayList<>();
    private final AuthService authService;

    public MyServer() {
        this.authService = new DataBaseMySqlAuthService();

    }

    public void start(int port) throws IOException {

            Selector selector = Selector.open();
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress("localhost", port));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server started");

            authService.start();
            //noinspection InfiniteLoopStatement
        try {
            while (true) {
                selector.select();
                System.out.println("New selector event");
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isAcceptable()) {
                        System.out.println("New selector acceptable event");
                        register(selector, serverSocket);
                    }

                    if (selectionKey.isReadable()) {
                        System.out.println("New selector readable event");
                        readMessage(selectionKey);
                    }
                    iterator.remove();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            authService.stop();
        }

    }

    public void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("New client is connected");
    }


    public void readMessage(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientHandler clientHandler = new ClientHandler(this, client);
        clientHandler.handle();
    }



    public synchronized void broadcastMessage(String message, ClientHandler sender) throws IOException {

        for (ClientHandler client : clients) {
            if (client == sender) {
                continue;
            }
            if (sender == null) {
                client.sendMessage(message);
            } else {
                client.sendMessage(sender.getNickname(), message);
            }

        }
    }

    public synchronized void subscribe(ClientHandler handler) throws IOException {
        clients.add(handler);
        notifyClientsUsersListUpdated(clients);
    }

    public synchronized void unsubscribe(ClientHandler handler) throws IOException {
        clients.remove(handler);
        notifyClientsUsersListUpdated(clients);
    }

    private void notifyClientsUsersListUpdated(List<ClientHandler> clients) throws IOException {
        List<String> usernames = new ArrayList<>();
        for (ClientHandler client : clients) {
            usernames.add(client.getNickname());
        }

        for (ClientHandler client : clients) {
//            List<String> usernames = clients.stream() Какойто- странный способ, может удалить его?
//                    .map(ClientHandler::getNickname)
//                    .collect(Collectors.toList());

            client.sendCommand(Command.updateUsersListCommand(usernames));
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isNickBusy(String nickname) {
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void sendPrivateMessage(ClientHandler sender, String recipient, String privateMessage) throws IOException {
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(recipient)) {
                client.sendMessage(sender.getNickname(), privateMessage);
            }
        }
    }
}