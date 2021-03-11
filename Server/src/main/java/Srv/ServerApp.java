package Srv;

import java.io.IOException;

public class ServerApp {

        private static final int DEFAULT_PORT = 9000;




        public static void main(String[] args) {
            int port = DEFAULT_PORT;
            if (args.length != 0) {
                port = Integer.parseInt(args[0]);
            }

            try {
                new MyServer().start(port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

