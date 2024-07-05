package server.auth;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

    private final String string;

    public Hash(String s) {
        string = s;
    }

    public String hash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(string.getBytes());
            byte[] hash = digest.digest();

            BigInteger number = new BigInteger(1, hash);

            StringBuilder hexString = new StringBuilder(number.toString(16));

            while (hexString.length() < 64) {
                hexString.insert(0, '0');
            }

            return hexString.toString();
        }
        catch (NoSuchAlgorithmException ignored) {
            return null;
        }
    }

}
