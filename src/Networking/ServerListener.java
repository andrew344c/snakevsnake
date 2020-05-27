package Networking;

import GameComponents.Cell;

import java.util.ArrayList;
import java.util.EventListener;

/**
 * Acts as direct communication for Server to GamePanel
 * @author Andrew
 */
public interface ServerListener extends EventListener {
    /**
     * Cell update occurs and needs to be updated in GUI
     * Only used in initialization phase
     * @param update updated cell
     */
    public void updateOccurred(Cell update);

    /**
     * Cell updates occur and need to be updated in GUI
     * Only used in spectate mode
     * @param update updated cells
     */
    public void updateOccurred(ArrayList<Cell> update);

    /**
     * GameStateEvent occurs
     * @param event a change in the state of the game (win, lose, tie)
     */
    public void gameStateEventOccurred(GameStateEvent event);
}
