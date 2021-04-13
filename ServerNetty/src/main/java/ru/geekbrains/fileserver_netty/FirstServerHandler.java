package ru.geekbrains.fileserver_netty;

import ClientServer.Command;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.geekbrains.fileserver_netty.Handler.ClientHandler;

import java.io.IOException;


public class FirstServerHandler extends SimpleChannelInboundHandler<Command> {

   private MyServer myServer;
   private ClientHandler client;

    public FirstServerHandler(MyServer myServer) {
        this.myServer = myServer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("New active channel");
        this.client = new ClientHandler(myServer,ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Command command) {
        //System.out.println(command);
        try {
            client.authentication(command);
            if(myServer.isClientExist(client)) {
                ctx.pipeline().remove(this);
                ctx.pipeline().addLast(new SecondServerHandler(myServer,client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }




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