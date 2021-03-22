package ClientServer.fileTransmitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class FileSender {

    public static void sendFile(SocketChannel socketChannel, String userFile) {
        RandomAccessFile aFile = null;
        try {
            File file = new File(userFile);
            aFile = new RandomAccessFile(file, "r");
            FileChannel inChannel = aFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (inChannel.read(buffer) > 0) {
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
            }
            System.out.println("End of file reached..");
            aFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}