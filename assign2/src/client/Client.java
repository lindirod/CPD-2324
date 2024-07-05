package client;

import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public abstract class Client implements Runnable {

    private final boolean printMessages;

    private State state = State.SEND;

    private final Socket socket;
    private BufferedReader socketReader;
    private PrintWriter socketWriter;

    private String messageSent;

    private String username;

    public Client(String host, int port, boolean pm) {
        try {
            socket = new Socket(host, port);
        }
        catch (IOException e) {
            throw new RuntimeException("Error creating client socket: ", e);
        }

        printMessages = pm;
    }

    private void initIO() {
        try {
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketWriter = new PrintWriter(socket.getOutputStream(), true);
        }
        catch (IOException e) {
            throw new RuntimeException("Error creating IO streams: ", e);
        }
    }

    private void closeIO() {
        try {
            socketReader.close();
            socketWriter.close();
            socket.close();
        }
        catch (IOException ignored) {

        }
    }

    @Override
    public void run() {
        initIO();

        while (state != State.EXIT) {
            switch (state) {
                case SEND -> sendMessage(getMessageToSend());
                case RECEIVE -> receiveMessage();
            }
        }

        closeIO();
    }

    protected abstract String getMessageToSend();

    private void sendMessage(String message) {
        String[] tokens = message.split(" ");

        if (tokens[0].equals("reconnect")) {
            username = tokens[1];
            socketWriter.println(tokens[0]);
        }
        else {
            socketWriter.println(message);
        }

        messageSent = message;

        state = State.RECEIVE;
    }

    private void receiveMessage() {
        try {
            String message = socketReader.readLine();
            if (message == null) {
                throw new RuntimeException("Error while communicating with server.");
            }

            parseMessage(message);
        }
        catch (IOException e) {
            throw new RuntimeException("Error while communicating with server: ", e);
        }
    }

    private void parseMessage(String message) {
        String[] tokens = message.split(" ");
        switch (tokens.length) {
            case 1 -> {
                switch (message) {
                    case "exit" -> state = State.EXIT;
                    case "pass" -> {
                        username = messageSent;
                        printMessage(messageToOutput(message));
                        state = State.SEND;
                    }
                    case "token" -> {
                        try {
                            List<String> lines = Files.readAllLines(Path.of(String.format("tokens/" + username + "token.txt")));
                            sendMessage(lines.getFirst());
                        }
                        catch (IOException e) {
                            sendMessage("null");
                        }
                    }
                    default -> {
                        printMessage(messageToOutput(message));
                        state = State.SEND;
                    }
                }
            }
            case 2 -> {
                if (tokens[0].equals("token")) {
                    try {
                        Files.createDirectories(Path.of("tokens/"));
                        Files.writeString(Path.of(String.format("tokens/" + username + "token.txt")),
                                tokens[1],
                                StandardOpenOption.CREATE);
                        sendMessage("ack");
                    }
                    catch (IOException e) {
                        throw new RuntimeException("Error while trying to save token: ", e);
                    }
                }
                else {
                    printMessage(messageToOutput(message));
                    state = State.SEND;
                }
            }
            case 3 -> {
                if (tokens[0].equals("reconnect") && tokens[1].equals("true")) {
                    switch (tokens[2]) {
                        case "authenticated" -> {
                            printMessage(
                                    "Reconnection successful. You're authenticated, the server is waiting for a message:");
                            state = State.SEND;
                        }
                        case "queued" -> {
                            printMessage(
                                    "Reconnection successful. You're queued, wait for another message from the server.");
                            state = State.RECEIVE;
                        }
                        case "in_game" -> {
                            printMessage("Reconnection successful. You're in a game, the server is waiting for a guess:");
                            state = State.SEND;
                        }
                    }
                }
            }
            case 8 -> {
                if (tokens[0].equals("place") && tokens[2].equals("players") && tokens[4].equals("score") && tokens[6].equals(
                        "answer")) {
                    printMessage(String.format("You placed %s out of %s players and were %s off the correct answer %s.",
                            tokens[1],
                            tokens[3],
                            tokens[5],
                            tokens[7]));
                    state = State.SEND;
                }
            }
            default -> throw new IllegalStateException("Unexpected message from server: " + message);
        }
    }

    private String messageToOutput(String message) {
        return switch (message) {
            case "pre help" -> """
                    login             Login with your credentials
                    signup            Sign up with new credentials (',' (comma) are not allowed)
                    reconnect <user>  Reconnect with <user>
                    exit              Exit client""";
            case "main help" -> """
                    logout  Logout
                    simple  Queue for a simple match
                    ranked  Queue for a ranked match
                    exit    Exit client""";
            case "user" -> "Type your username:";
            case "pass" -> "Type your password:";
            case "login true" -> "Login successful.";
            case "login false" -> "Login failed.";
            case "signup true" -> "Signup successful.";
            case "signup false" -> "Signup failed.";
            case "reconnect false" -> "Reconnection failed.";
            case "logout true" -> "Logout successful.";
            case "logout false" -> "Logout failed.";
            case "guess" -> "Take your guess:";
            case "again" -> "Invalid guess. Please try again:";
            case "invalid" -> "Invalid command. Type 'help' if you need a list of commands.";
            default -> throw new IllegalStateException("Unexpected message from server: " + message);
        };
    }

    private void printMessage(String message) {
        if (printMessages) {
            System.out.println(message);
        }
    }

    private enum State {

        SEND,
        RECEIVE,
        EXIT

    }

}
