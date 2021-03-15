package server;

import server.Handler.ClientHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;


public class MyServer {

    private final List<ClientHandler> clients = new ArrayList<>();
    private final AuthService authService;
    private Selector selector;
    private ServerSocketChannel serverSocket;


    public MyServer() {
        this.authService = new DataBaseMySqlAuthService();

    }

    public void start(int port) throws IOException {
            selector = Selector.open();
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress("localhost", port));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server started");
            authService.start();

        try {
            while (true) {
                selector.select();

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {

                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isAcceptable()) {
                        System.out.println("New selector event");
                        System.out.println("New selector acceptable event");
                        register(selector, serverSocket);
                    }

                    if (selectionKey.isReadable()) {
                        readMessage(selectionKey);
                    }
                    iterator.remove();
                }

            }
        } catch (IOException e) {
            System.out.println("Выход из цикла селектора");
            e.printStackTrace();
        } finally {
            authService.stop();
            serverSocket.close();
            selector.close();
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


   /* private void runServerMessageThread() {
        Thread serverMessageThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String serverMessage = scanner.next();

                    if(serverMessage.equals("exit")) {
                        System.out.println("Exit");
                     exit = true;
                     stop();
                    }
            }
        });
        serverMessageThread.setDaemon(true);
        serverMessageThread.start();
    }*/


    public synchronized void subscribe(ClientHandler handler) throws IOException {
        clients.add(handler);

    }

    public synchronized void unsubscribe(ClientHandler handler) throws IOException {
        clients.remove(handler);
    }


    public AuthService getAuthService() {
        return authService;
    }


}