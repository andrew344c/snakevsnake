package Networking;

import GameComponents.Cell;

import java.util.EventListener;

public interface ChatListener extends EventListener {
    public void chatEventOccurred(ChatEvent chatEvent);
}
