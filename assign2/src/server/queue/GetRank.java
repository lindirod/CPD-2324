package server.queue;

import java.util.function.Function;

import server.Client;

public class GetRank implements Function<Client, Integer> {

    @Override
    public Integer apply(Client client) {
        return client.getUser().getRank();
    }

}
