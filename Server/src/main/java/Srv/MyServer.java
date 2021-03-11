package Srv;

import Srv.Handler.ClientHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MyServer {

    private final List<ClientHandler> clients = new ArrayList<>();
    private final AuthService authService;

    public MyServer() {
        this.authService = new DataBaseMySqlAuthService();
      }

    public void start(int port) throws Exception {

        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress("localhost", 9000));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started");
        authService.start();
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
    }

    public void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("New client is connected");
    }


    public void readMessage(SelectionKey key) throws Exception {
        SocketChannel client = (SocketChannel) key.channel();
        ClientHandler clientHandler = new ClientHandler(this, client);
        clientHandler.handle();
    }



    public AuthService getAuthService() {
        return authService;
    }

    public synchronized void subscribe(ClientHandler handler) throws IOException {
        clients.add(handler);

    }

    public synchronized void unsubscribe(ClientHandler handler) throws IOException {
        clients.remove(handler);

    }


    public boolean isNickBusy(String nickname) {
        return true;
    }

    public void sendPrivateMessage(ClientHandler clientHandler, String receiver, String message) {
    }
}