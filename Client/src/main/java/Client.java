import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private final static ExecutorService THREAD_POOL = Executors.newFixedThreadPool(5);

    public static void main(String[] args) {
        try {
            new Client().start();
        } finally {
            THREAD_POOL.shutdown();
        }
    }

    public void start() {
        for (int i = 0; i < 5; i++) {
            THREAD_POOL.execute(() -> {
                System.out.println("New client started on thread " + Thread.currentThread().getName());
                try {
                    SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost", 9000));
                    while (true) {
                        channel.write(ByteBuffer.wrap(String.format(
                                "[%s] Message from thread %s",
                                LocalDateTime.now(),
                                Thread.currentThread().getName()
                        ).getBytes()));
                        Thread.sleep(1000);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}