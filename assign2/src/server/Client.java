package server;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.TimeUnit;

import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

import server.auth.User;

public class Client implements Runnable {

    private final Server server;

    public State state = State.UNAUTHENTICATED;
    private boolean isBroken = false;
    public Client sub;

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    private Socket socket;
    private BufferedReader socketReader;
    private PrintWriter socketWriter;

    private User user;
    private String token;

    public Client(Server s, Socket cs) {
        server = s;
        socket = cs;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket s) {
        closeIO();

        socket = s;
        if (!initIO()) state = State.EXIT;
    }

    private boolean initIO() {
        try {
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketWriter = new PrintWriter(socket.getOutputStream(), true);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public void closeIO() {
        try {
            socketReader.close();
            socketWriter.close();
            socket.close();
        }
        catch (IOException ignored) {

        }
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    @Override
    public void run() {
        if (!initIO()) return;

        server.addClient(this);

        while (server.isRunning() && state != State.EXIT) {
            switch (state) {
                case UNAUTHENTICATED -> preLoop();
                case AUTHENTICATED -> mainLoop();
                case QUEUED -> await(state);
            }
        }

        server.removeClient(this);
    }

    public String readMessage() throws IllegalStateException {
        try {
            return socketReader.readLine();
        }
        catch (IOException e) {
            isBroken = true;

            while (isBroken) {
                try {
                    lock.lock();
                    if (!condition.await(2, TimeUnit.MINUTES)) {
                        state = State.EXIT;
                        return null;
                    }
                    lock.unlock();
                }
                catch (InterruptedException ignored) {

                }
            }
            return readMessage();
        }
    }

    public void writeMessage(String message) {
        socketWriter.println(message);
    }

    private void preLoop() {
        switch (readMessage()) {
            case "help" -> preHelp();
            case "login" -> login();
            case "signup" -> signup();
            case "reconnect" -> reconnect();
            case "exit" -> exit();
            case null, default -> invalid();
        }
    }

    private void preHelp() {
        writeMessage("pre help");
    }

    private void login() {
        writeMessage("user");
        String username = readMessage();

        writeMessage("pass");
        String password = readMessage();

        User u = server.authenticateUser(username, password);
        authenticate(u, "login");
    }

    private void signup() {
        writeMessage("user");
        String username = readMessage();

        writeMessage("pass");
        String password = readMessage();

        if (username.contains(",") || password.contains(",")) {
            writeMessage("signup false");
        }
        else {
            User u = server.registerUser(username, password);
            authenticate(u, "signup");
        }
    }

    private void reconnect() {
        writeMessage("token");
        String token = readMessage();

        User u = server.reconnectUser(this, token);
        authenticate(u, "reconnect");
    }

    private void authenticate(User u, String action) {
        if (u == null) {
            writeMessage(String.format("%s false", action));
        }
        else {
            token = server.generateToken(u.getUsername());
            do {
                writeMessage(String.format("token %s", token));
            }
            while (!readMessage().equals("ack"));

            user = u;

            if (action.equals("login") || action.equals("signup")) {
                writeMessage(String.format("%s true", action));
                state = State.AUTHENTICATED;
            }
            else if (action.equals("reconnect")) {
                writeMessage(String.format("%s true %s", action, state.toString().toLowerCase()));

                sub.token = token;

                sub.isBroken = false;

                sub.lock.lock();
                sub.condition.signal();
                sub.lock.unlock();

                state = State.EXIT;
            }
        }
    }

    private void mainLoop() {
        switch (readMessage()) {
            case "help" -> mainHelp();
            case "logout" -> logout();
            case "simple" -> simple();
            case "ranked" -> ranked();
            case "exit" -> exit();
            case null, default -> invalid();
        }
    }

    private void mainHelp() {
        writeMessage("main help");
    }

    private void logout() {
        user = null;
        token = "";
        writeMessage("logout true");

        state = State.UNAUTHENTICATED;
    }

    private void simple() {
        server.queueSimple(this);

        state = State.QUEUED;
    }

    private void ranked() {
        server.queueRanked(this);

        state = State.QUEUED;
    }

    private void exit() {
        server.removeClient(this);
        user = null;
        writeMessage("exit");
        closeIO();

        state = State.EXIT;
    }

    private void invalid() {
        writeMessage("invalid");
    }

    private synchronized void await(State s) {
        while (s == state) {
            try {
                wait();
            }
            catch (InterruptedException ignored) {

            }
        }
    }

    public enum State {

        UNAUTHENTICATED,
        AUTHENTICATED,
        QUEUED,
        IN_GAME,
        EXIT

    }

}
