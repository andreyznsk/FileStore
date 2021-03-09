import java.io.IOException;

public class ServerApp {
    private static MyServer server;
    public static void main(String[] args) {
        try {
            server = new MyServer();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
