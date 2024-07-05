package server.queue;

import server.Server;
import server.Client;
import server.game.Game;

import java.util.List;
import java.util.LinkedList;

public class QueueManager implements Runnable {

    private final Server server;

    private final BarrierQueue<Client> queue;

    private final boolean isRanked;

    public QueueManager(Server s, BarrierQueue<Client> q, boolean ir) {
        server = s;
        queue = q;
        isRanked = ir;
    }

    @Override
    public void run() {
        while (server.isRunning()) {
            List<Client> players = new LinkedList<>();

            try {
                queue.drainTo(players);
            }
            catch (InterruptedException ignored) {
                continue;
            }

            Thread.startVirtualThread(new Game(server, players, isRanked));

            String messageGame = String.format("%s game started with players: ", isRanked ? "Ranked" : "Simple");
            String messagePlayers = String.join(" ", players.stream().map(p -> p.getUser().getUsername()).toList());

            synchronized (System.out) {
                System.out.println(messageGame + messagePlayers + '.');
            }
        }
    }

    public void enqueue(Client client) {
        queue.put(client);
    }

}
