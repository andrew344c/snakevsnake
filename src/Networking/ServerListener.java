package Networking;

import GameComponents.Cell;

import java.util.ArrayList;
import java.util.EventListener;

/**
 * Acts as direct communication for Server to GamePanel
 * @author Andrew
 */
public interface ServerListener extends EventListener {
    public void updateOccurred(Cell update);
    public void updateOccurred(ArrayList<Cell> update);
    public void gameStateEventOccurred(GameStateEvent event);
}
