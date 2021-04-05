package ru.geekbrains.fileserver_netty;

import ClientServer.Command;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.geekbrains.fileserver_netty.Handler.ClientHandler;

import java.io.IOException;


public class SecondServerHandler extends SimpleChannelInboundHandler<Command> {

    private MyServer myServer;
    private ClientHandler client;

    public SecondServerHandler(MyServer myServer,ClientHandler client ) {
        this.myServer = myServer;
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("New active channel");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Command command) {
        System.out.println(command);
        client.processClient(command);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client disconnect");
    }

}