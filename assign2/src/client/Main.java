package client;

import java.util.List;
import java.util.LinkedList;
import java.util.stream.Stream;
import java.util.concurrent.TimeUnit;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        switch (args.length) {
            case 2 -> {
                String host = args[0];
                int port = Integer.parseInt(args[1]);

                Thread.startVirtualThread(new ManualClient(host, port)).join();
            }
            case 3 -> {
                String host = args[0];
                int port = Integer.parseInt(args[1]);
                if (!args[2].equals("bots")) return;

                Thread.startVirtualThread(() -> {
                    List<Thread> threads = new LinkedList<>();
                    Stream.iterate(0, n -> n + 1).limit(100).forEach(i -> {
                        threads.add(Thread.startVirtualThread(new AutoClient(host,
                                port,
                                "login",
                                String.format("user%d", i),
                                "1234")));
                        System.out.printf("%s connected%n", "user" + i);
                        try {
                            TimeUnit.SECONDS.sleep(new Random().nextInt(1, 10));
                        }
                        catch (InterruptedException ignored) {

                        }
                    });

                    for (Thread t : threads) {
                        try {
                            t.join();
                        }
                        catch (InterruptedException ignored) {

                        }
                    }
                }).join();
            }
        }
    }

}
