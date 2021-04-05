package ru.geekbrains.fileserver_netty;

import ClientServer.Command;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.commons.lang3.SerializationUtils;

import java.util.List;

public class CommandEncoder extends MessageToMessageEncoder<Command> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Command command, List<Object> out) throws Exception {
        System.out.println("Посылем команду: "+command.getType());
        byte[] bytes = SerializationUtils.serialize(command);
        out.add(bytes);
    }
}
