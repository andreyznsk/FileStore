package ru.geekbrains.fileserver_netty;

import ClientServer.Command;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.apache.commons.lang3.SerializationUtils;

import java.util.List;

public class CommandDecoder extends MessageToMessageDecoder<byte[]> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, byte[] bytes, List<Object> out) throws Exception {
       Command command;
        command = SerializationUtils.deserialize(bytes);
        out.add(command);
        //System.out.println(command.getData().toString());

    }
}
