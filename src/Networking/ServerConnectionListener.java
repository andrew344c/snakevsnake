package Networking;

import java.util.EventListener;

public interface ServerConnectionListener extends EventListener {
    public void connectionEventOccurred(ServerConnectionEvent event);
}
