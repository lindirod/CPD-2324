# Assignment 2

## Java Environment

To run the server or client you must have Java SE 21 or later.

## Server

To run the server execute this command while on the src directory:

```bash
$ java server.Main <port> [players_per_game]
```

- `port` is the number of the port to which the server socket will be bound to
- `players_per_game` is the number is the number of players each game will start with (between 1 and 32), 5 when not specified

The server will print relevant information like a client connection or a game start and can be stopped by typing stop in the console.

## Clients

To run the clients execute this command while on the src directory:

```bash
$ java client.Main <host> <port> [bots]
```

- `host` is the host to which the client socket will connect to, should be `localhost` for running locally
- `port` is the port to which the client socket will connect to on the named host, should be the same as the server's
- `bots` specifies whether to run automatic clients or a manual user-controlled client, must be either the string `bots` or unspecified

Right after launching a manual client, you may type in a command. You can type help at any point to know which commands are possible.

While unauthenticated you can:

- `login` which initiates a sequence of messages with the server in which you should enter your credentials
- `signup` which initiates a sequence of messages with the server in which you can create new credentials
- `reconnect <user>` which attempts to reconnect with `user` username
- `exit` which exits the client and closes the connection with the server gracefully

While authenticated you can:

- `logout` which initiates restores the state of the connection to unauthenticated
- `simple` which enqueues you for a simple match which assigns players to games by order of entry and does not affect your rank
- `ranked` which enqueues you for a ranked match which assigns players to games based on their current rank and time elapsed since enqueue and may affect your rank
- `exit` logs out, exits the client and closes the connection with the server gracefully

If a manual client terminates ungracefully and no more than 2 minutes have elapsed since the server noticed the disconnection, you can reconnect and retake your place in the queue you might have been waiting at by launching the manual client once again and using the reconnect command with your username.
