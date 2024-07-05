package server.game;

import server.Server;
import server.Client;

public class Player implements Runnable {

    private final Server server;
    private final Client client;
    private final Game game;

    private final boolean isRanked;

    private final Latch guessLatch;

    public Player(Server s, Client c, Game g, Latch gl, boolean ir) {
        server = s;
        client = c;
        game = g;
        guessLatch = gl;
        isRanked = ir;
    }

    @Override
    public void run() {
        client.state = Client.State.IN_GAME;

        client.writeMessage("guess");
        int guess = toValidGuess(client.readMessage());
        while (guess < 0) {
            if (guess == -1) {
                client.writeMessage("again");
                guess = toValidGuess(client.readMessage());
            }
            else if (guess == -2) {
                guessLatch.countDown();
                return;
            }
        }

        int score = game.score(guess);

        guessLatch.countDown();
        try {
            guessLatch.await();
        }
        catch (InterruptedException ignored) {

        }

        int place = game.place(score);
        int numberScores = game.numberScores();

        client.writeMessage(String.format("place %d players %d score %d answer %d",
                place,
                numberScores,
                score,
                game.getAnswer()));

        if (isRanked) {
            server.updateRank(client.getUser(), numberScores / 2 - place + 1);
        }

        synchronized (client) {
            client.state = Client.State.AUTHENTICATED;
            client.notify();
        }
    }

    private static int toValidGuess(String guess) {
        if (guess == null) {
            return -2; // -2 means that the game should stop trying to get a valid guess
        }

        try {
            int g = Integer.parseInt(guess);

            if (g >= 0) {
                return g;
            }
        }
        catch (NumberFormatException ignored) {

        }

        return -1; // -1 means that the game should keep trying to get a valid guess
    }

}
