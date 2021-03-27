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

    private final List<ClientHandler> clients = new ArrayList<>();//список текщих поключенных и авторизованных клиентов
    private final AuthService authService;//Ссылка на подключенный класс реализующий сервис авторизации
    private SocketChannel client;
    public int serverPort;
    public String serverHost;
    Map<SocketChannel,ClientHandler> clientsChannelsHandlers = new HashMap<>();//Мап каналов и их обработчиков

    public MyServer() {
        this.authService = new DataBaseMySqlAuthService();

    }

    public void start(String host, int port) throws IOException {
            serverPort = port;
            serverHost = host;
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(host, port));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server started");
            authService.start();

        try (selector; serverSocket) {
            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();

                    if (selectionKey.isValid()) {
                        if (selectionKey.isAcceptable()) {
                            register(selector, serverSocket);
                        } else if (selectionKey.isConnectable()) {
                            SocketChannel sock = (SocketChannel) selectionKey.channel();
                            sock.finishConnect();
                            continue;
                        }
                        if (selectionKey.isReadable()) {
                            readMessage(selectionKey);
                        }
                        iterator.remove();
                    }
                }

            }
        } catch (IOException e) {
            System.out.println("Выход из цикла селектора");
            e.printStackTrace();
        } finally {
            authService.stop();
        }

    }

    public void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("New client is connected");
    }

    /**
     * Метод обработки подключенных клиентов, вызов соответствующего обработчика
     * @param key селектор
     * @throws IOException
     */
    public void readMessage(SelectionKey key) throws IOException {
        client = (SocketChannel) key.channel();
        ClientHandler clientHandler;
        if(!clientsChannelsHandlers.containsKey(client)) {//Если данный клиент ниразу не подключался
            clientHandler = new ClientHandler(this, client);//Создать новый экземпляр обработчика команд
            clientsChannelsHandlers.put(client, clientHandler);//Ассоциировать экземпляр обработчика с клиентом
        } else {
            clientHandler = clientsChannelsHandlers.get(client);//Если клиент уже подключался, то достать обработчик по клиенту
        }
        if(clients.contains(clientHandler)) clientHandler.processClient();//Если клиент прошел аутентификацию запустить метод чтения команд
        else clientHandler.authentication();//иначе запустить аутентификацию

    }

    /**
     * Метод проверки подключившихся клиентов, для исключения подключения одного и того же клиента с одним и тем же ником.
     * @param nickname Запрашиваемый ник
     * @return
     */
    public synchronized boolean isNickBusy(String nickname) {
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Если клиент авторизовался, то добавить в список текщих поключенных и авторизованных клиентов
     * @param handler
     */
   public synchronized void subscribe(ClientHandler handler) {
        clients.add(handler);
    }

    /**
     * Если клиент отключился, удалить из списка авторизованных
     * и удалить ключ-значение обработчиков каналов
     * @throws IOException
     */
    public synchronized void unsubscribe(ClientHandler handler) throws IOException {
       clientsChannelsHandlers.remove(client);
       clients.remove(handler);
    }

    public AuthService getAuthService() {
        return authService;
    }


}