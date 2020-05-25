package Networking;

import GameComponents.Cell;

import java.util.EventListener;

public interface UpdateListener extends EventListener {
    public void updateOccurred(Cell update);
    public void specialEventOccurred(SpecialEvent event);
}
