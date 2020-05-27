package Networking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventObject;

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

    public GameStateEvent(Object source, int type) {
        super(source);
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setPlayers(ArrayList<String> players) {
        this.players = players;
    }

    public ArrayList<String> getPlayers() {
        return players;
    }

}
