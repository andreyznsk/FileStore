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

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}