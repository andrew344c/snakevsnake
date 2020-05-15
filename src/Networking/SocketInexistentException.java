package Networking;

public class SocketInexistentException extends Exception {
    public SocketInexistentException(String ip, int port) {
        super(String.format("The server at {IP: %1$s; PORT: %2$s} doesn't exist", ip, port));
    }
}
