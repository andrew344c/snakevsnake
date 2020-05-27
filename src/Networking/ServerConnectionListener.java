package Networking;

import java.util.EventListener;

/**
 * Listens for changes in the connection between the server and a client
 */
public interface ServerConnectionListener extends EventListener {
    /**
     * Change in the connection occurs
     * @param event holds information of the type of change
     */
    public void connectionEventOccurred(ServerConnectionEvent event);
}
