package server;

import java.io.IOException;

public class ServerApp {

        private static final int DEFAULT_PORT = 9000;
        private static final String DEFAULT_HOST = "localhost";




        public static void main(String[] args) {
            int port = DEFAULT_PORT;
            String host = DEFAULT_HOST;
            if (args.length != 0) {
                port = Integer.parseInt(args[0]);
                host = args[1];
            }

            try {
                new MyServer().start(host, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

