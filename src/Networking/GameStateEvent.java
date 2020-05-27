package Networking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventObject;

/**
 * A event symbolizing a change in the state of the game (win, lose, tie)
 */
public class GameStateEvent extends EventObject implements Serializable {

    private int type;   // Type of special event
    private ArrayList<String> players;  // Used if special event is Lost, Tie, Win; Specifies who lost/tie/won

    // The types of special events
    public static final int START = 0;
    public static final int END = 1;
    public static final int LOST = 2;
    public static final int WIN = 3;
    public static final int TIE = 4;
    public static final int READY = 5;

    /**
     * Constructor
     * @param source caller of event
     * @param type type of Game State Event
     */
    public GameStateEvent(Object source, int type) {
        super(source);
        this.type = type;
    }

    /**
     * Gets type of game state event
     * @return type the type of event it is
     */
    public int getType() {
        return type;
    }

    /**
     * Set players in accordance to event
     * @param players only used in tie and win
     */
    public void setPlayers(ArrayList<String> players) {
        this.players = players;
    }

    /**
     * Gets player in accordance to event
     * @return players that won/tied
     */
    public ArrayList<String> getPlayers() {
        return players;
    }

}
