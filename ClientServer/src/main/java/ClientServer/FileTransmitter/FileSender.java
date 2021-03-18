package ClientServer.FileTransmitter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;

public class FileSender {

    private final InetSocketAddress hostAddress;
    private SocketChannel client;

    public FileSender(final int port, SocketChannel client) throws IOException {
        this.hostAddress = new InetSocketAddress(port);
        this.client = client;
    }

    void transfer(final FileChannel channel, long position, long size) throws IOException {
        assert !Objects.isNull(channel);
        while (position < size) {
            position += channel.transferTo(position, 1024, this.client);
        }
    }

    SocketChannel getChannel() {
        return this.client;
    }

    void close() throws IOException {
        this.client.close();
    }
}