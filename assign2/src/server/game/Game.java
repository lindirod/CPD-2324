package server.game;

import server.Server;
import server.Client;

import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import java.util.Random;

public class Game implements Runnable {

    public static final int ANSWER_BOUND = 50;

    private final Server server;
    private final List<Client> clients;

    private final boolean isRanked;

    private int answer;
    private final List<Integer> scores = new LinkedList<>();

    public Game(Server s, List<Client> c, boolean ir) {
        server = s;
        clients = c;
        isRanked = ir;
    }

    @Override
    public void run() {
        answer = new Random().nextInt(ANSWER_BOUND);

        Latch guessLatch = new Latch(clients.size());

        for (Client client : clients) {
            Thread.startVirtualThread(new Player(server, client, this, guessLatch, isRanked));
        }
    }

    public int getAnswer() {
        return answer;
    }

    public int score(int n) {
        int score = Math.abs(answer - n);

        synchronized (scores) {
            scores.add(score);
            Collections.sort(scores);
        }

        return score;
    }

    public int place(int score) {
        synchronized (scores) {
            for (int i = 0; i < scores.size(); ++i) {
                if (scores.get(i) == score) {
                    return i + 1;
                }
            }
        }

        return 0;
    }

    public int numberScores() {
        synchronized (scores) {
            return scores.size();
        }
    }

}
