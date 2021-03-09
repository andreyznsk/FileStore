
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;


public class MyServer {
    private DatagramChannel server;
    private InetSocketAddress iAdd;
    private SocketAddress remoteAdd;

    public MyServer() throws IOException {
        server = DatagramChannel.open();
        iAdd = new InetSocketAddress("localhost", 8989);
        server.bind(iAdd);
        System.out.println("Server Started: " + iAdd);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //receive buffer from client.
        this.remoteAdd = server.receive(buffer);
        //change mode of buffer
        buffer.flip();
        int limits = buffer.limit();
        byte bytes[] = new byte[limits];
        buffer.get(bytes, 0, limits);
        String msg = new String(bytes);
        System.out.println("Client at " + remoteAdd + "  sent: " + msg);
        server.send(buffer,remoteAdd);
        server.close();
    }

}
