package client;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AutoClient extends Client {

    private final Random random = new Random();

    private final String authAction;
    private final String username;
    private final String password;

    private State state = State.AUTH_ACTION;

    private int numberGames;

    public AutoClient(String host, int port, String aa, String u, String p) {
        super(host, port, false);

        authAction = aa;
        username = u;
        password = p;

        numberGames = random.nextInt(100);
    }

    @Override
    protected String getMessageToSend() {
        return switch (state) {
            case AUTH_ACTION -> {
                state = State.USERNAME;
                yield authAction;
            }
            case USERNAME -> {
                state = State.PASSWORD;
                yield username;
            }
            case PASSWORD -> {
                state = State.QUEUE;
                yield password;
            }
            case QUEUE -> {
                state = State.GUESS;

                sleepForSeconds(random.nextInt(10));

                yield random.nextBoolean() ? "simple" : "ranked";
            }
            case GUESS -> {
                state = numberGames-- < 0 ? State.EXIT : State.QUEUE;

                sleepForSeconds(random.nextInt(5));

                yield Integer.toString(random.nextInt(50));
            }
            case EXIT -> "exit";
        };
    }

    private void sleepForSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        }
        catch (InterruptedException ignored) {

        }
    }

    private enum State {

        AUTH_ACTION,
        USERNAME,
        PASSWORD,
        QUEUE,
        GUESS,
        EXIT

    }

}
