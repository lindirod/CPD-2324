package server.auth;

public class User {

    private final String username;
    private final String hashedPassword;
    private int rank;

    public User(String u, String p, int r, boolean isHashed) {
        username = u;
        hashedPassword = isHashed ? p : hash(p);
        rank = r;
    }

    public int getRank() {
        return rank;
    }

    public void updateRank(int inc) {
        rank += inc;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public boolean checkUsername(String string) {
        return username.equals(string);
    }

    public boolean checkPassword(String string) {
        return hashedPassword.equals(hash(string));
    }

    private static String hash(String string) {
        Hash hash = new Hash(string);
        return hash.hash();
    }

}
