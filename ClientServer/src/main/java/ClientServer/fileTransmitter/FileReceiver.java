package ClientServer.fileTransmitter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


public class FileReceiver {
//   private ServerSocketChannel serverSocketChannel = null;

/*
    public SocketChannel createServerSocketChannel(SocketAddress socketAddress) {


        SocketChannel socketChannel = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(socketAddress);
            socketChannel = serverSocketChannel.accept();
            System.out.println("Connection established...." + socketChannel.getRemoteAddress());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return socketChannel;
    }
*/

    /**
     * Reads the bytes from socket and writes to file
     *
     * @param socketChannel
     */
    public static void readFileFromSocket(SocketChannel socketChannel, String userServerPath) {
        RandomAccessFile aFile = null;
        try {
            aFile = new RandomAccessFile(userServerPath, "rw");
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            FileChannel fileChannel = aFile.getChannel();
            while (socketChannel.read(buffer) > 0) {
                buffer.flip();
                fileChannel.write(buffer);
                buffer.clear();
            }
            fileChannel.close();
            System.out.println("End of file reached..Closing channel");
            //serverSocketChannel.close();
            //socketChannel.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}