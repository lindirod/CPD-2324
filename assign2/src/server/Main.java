package server;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        Server server;
        switch (args.length) {
            case 1 -> {
                int port = Integer.parseInt(args[0]);

                server = new Server(port, 5);
            }
            case 2 -> {
                int port = Integer.parseInt(args[0]);
                int playersPerGame = Integer.parseInt(args[1]);

                if (playersPerGame < 1 || playersPerGame > 32)
                    throw new IllegalArgumentException("The number of players per game should be between 1 and 32");

                server = new Server(port, playersPerGame);
            }
            default -> {
                return;
            }
        }

        Thread.startVirtualThread(server);

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                if (inputReader.readLine().equals("stop")) {
                    server.stop();
                    break;
                }
            }
            catch (IOException e) {
                inputReader = new BufferedReader(new InputStreamReader(System.in));
            }
        }
    }

}
