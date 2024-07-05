package server;

import java.net.ServerSocket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

import java.util.List;
import java.util.LinkedList;

import server.auth.User;
import server.auth.Hash;

import server.queue.QueueManager;
import server.queue.RankedQueue;
import server.queue.SimpleQueue;
import server.queue.GetRank;

import java.util.concurrent.TimeUnit;

public class Server implements Runnable {

    private static final int INITIAL_TOLERANCE = 10;
    private static final int TOLERANCE_INCREMENT_INTERVAL_SECONDS = 20;

    private static final Path USERS_CSV = Path.of("users.csv");
    private final Object usersFileLock = new Object();

    private ServerSocket serverSocket;

    private boolean isRunning;

    private final List<User> users = new LinkedList<>();
    private final List<Client> clients = new LinkedList<>();
    private Integer token = 0;

    private final QueueManager simpleManager;
    private final QueueManager rankedManager;

    public Server(int port, int playersPerGame) {
        createServerSocket(port);

        synchronized (usersFileLock) {
            try (Stream<String> lines = Files.lines(USERS_CSV)) {
                lines.forEach(line -> {
                    String[] tokens = line.split(",");
                    users.add(new User(tokens[0], tokens[1], Integer.parseInt(tokens[2]), true));
                });
            }
            catch (IOException e) {
                throw new RuntimeException("Error when reading users file: ", e);
            }
        }

        simpleManager = new QueueManager(this, new SimpleQueue<>(playersPerGame), false);
        rankedManager = new QueueManager(this,
                new RankedQueue<>(playersPerGame,
                        new GetRank(),
                        INITIAL_TOLERANCE,
                        TOLERANCE_INCREMENT_INTERVAL_SECONDS,
                        TimeUnit.SECONDS),
                true);

        isRunning = true;
    }

    @Override
    public void run() {
        Thread.startVirtualThread(simpleManager);
        Thread.startVirtualThread(rankedManager);

        while (isRunning()) {
            try {
                Thread.startVirtualThread(new Client(this, serverSocket.accept()));
            }
            catch (IOException e) {
                if (serverSocket.isClosed()) {
                    createServerSocket(serverSocket.getLocalPort());
                }
                else {
                    throw new RuntimeException("IO Error occurred with serverSocket: ", e);
                }
            }
        }
    }

    private void createServerSocket(int port) {
        try {
            serverSocket = new ServerSocket(port);
            synchronized (System.out) {
                System.out.println("Server started on port " + port + '.');
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error creating server socket: ", e);
        }
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    public synchronized void stop() {
        isRunning = false;

        for (Client client : clients) {
            client.closeIO();
        }

        try {
            serverSocket.close();
        }
        catch (IOException ignored) {

        }
    }

    public User authenticateUser(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        synchronized (clients) {
            for (Client client : clients) {
                User user = client.getUser();
                if (user == null) continue;

                String u = client.getUser().getUsername();
                if (u == null) continue;

                if (u.equals(username)) {
                    return null;
                }
            }
        }

        synchronized (users) {
            for (User user : users) {
                if (user.checkUsername(username) && user.checkPassword(password)) {
                    return user;
                }
            }
        }

        return null;
    }

    public User registerUser(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        synchronized (users) {
            for (User u : users) {
                if (u.checkUsername(username)) {
                    return null;
                }
            }

            User u = new User(username, password, 0, false);

            if (!preserveUser(u)) return null;

            users.add(u);

            return u;
        }
    }

    private boolean preserveUser(User user) {
        synchronized (usersFileLock) {
            try {
                Files.writeString(USERS_CSV,
                        String.join(",",
                                user.getUsername(),
                                user.getHashedPassword(),
                                Integer.toString(user.getRank())) + '\n',
                        StandardOpenOption.APPEND);
                return true;
            }
            catch (IOException e) {
                return false;
            }
        }
    }

    public User reconnectUser(Client client, String token) {
        if (token == null) {
            return null;
        }

        synchronized (clients) {
            for (Client c : clients) {
                if (c.getToken() != null && c.getToken().equals(token)) {
                    c.setSocket(client.getSocket());
                    client.state = c.state;
                    client.sub = c;
                    return c.getUser();
                }
            }
        }

        return null;
    }

    public void updateRank(User user, int inc) {
        if (inc == 0) return;

        user.updateRank(inc);

        synchronized (usersFileLock) {
            try (Stream<String> lines = Files.lines(USERS_CSV)) {
                List<String> newLines = lines.map(line -> {
                    String[] tokens = line.split(",");

                    if (tokens[0].equals(user.getUsername())) {
                        int newRank = Integer.parseInt(tokens[2]) + inc;
                        return String.join(",", tokens[0], tokens[1], Integer.toString(newRank));
                    }
                    else {
                        return line;
                    }
                }).toList();

                Files.write(USERS_CSV, newLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            catch (IOException ignored) {

            }
        }
    }

    public String generateToken(String username) {
        Hash hash = new Hash(username + token++);
        return hash.hash();
    }

    public void addClient(Client client) {
        synchronized (clients) {
            clients.add(client);
        }
        synchronized (System.out) {
            System.out.println("Client connected on port " + client.getSocket().getPort() + '.');
        }
    }

    public void removeClient(Client client) {
        synchronized (clients) {
            clients.remove(client);
        }
    }

    public void queueSimple(Client client) {
        simpleManager.enqueue(client);
    }

    public void queueRanked(Client client) {
        rankedManager.enqueue(client);
    }

}
