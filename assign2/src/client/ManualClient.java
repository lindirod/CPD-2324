package client;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class ManualClient extends Client {

    private final BufferedReader inputReader;

    public ManualClient(String host, int port) {
        super(host, port, true);

        inputReader = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    protected String getMessageToSend() {
        try {
            return inputReader.readLine();
        }
        catch (IOException e) {
            throw new RuntimeException("Error getting input from user: ", e);
        }
    }

}
