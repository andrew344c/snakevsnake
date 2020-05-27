package Networking;

import GameComponents.Cell;

import java.util.EventListener;

/**
 * Simple interface used by the client for listening for chat events from the server and directly communicating
 * with the GUI
 */
public interface ChatListener extends EventListener {
    /**
     * A chat event is received
     * @param chatEvent chat message
     */
    public void chatEventOccurred(ChatEvent chatEvent);
}
