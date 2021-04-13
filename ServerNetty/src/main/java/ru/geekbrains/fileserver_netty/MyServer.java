package ru.geekbrains.fileserver_netty;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import ru.geekbrains.fileserver_netty.Handler.ClientHandler;

import java.io.IOException;
import java.util.*;


public class MyServer {

    private final List<ClientHandler> clients = new ArrayList<>();//список текщих поключенных и авторизованных клиентов
    private final AuthService authService = new DataBaseMySqlAuthService();//Ссылка на подключенный класс реализующий сервис авторизации
    //private SocketChannel client;
   // Map<SocketChannel,ClientHandler> clientsChannelsHandlers = new HashMap<>();//Мап каналов и их обработчиков

    public MyServer() {
        //this.authService = new DataBaseMySqlAuthService();

    }

    public void start(String host, int port) throws InterruptedException {
        authService.start();

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(0);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LineBasedFrameDecoder(1024*1024),
                                    new ByteArrayDecoder(),
                                    new ByteArrayEncoder(),
                                    new CommandDecoder(),
                                    new CommandEncoder(),
                                    new FirstServerHandler(MyServer.this));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = server.bind(port).sync();

            System.out.println("Server started");

            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

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
       clients.remove(handler);
    }

    public AuthService getAuthService() {
        return authService;
    }


    public boolean isClientExist(ClientHandler client) {
        return clients.contains(client);
    }
}