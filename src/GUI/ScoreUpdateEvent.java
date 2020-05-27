package GUI;

import java.util.EventObject;

/**
 * Represents an update in the score
 */
public class ScoreUpdateEvent extends EventObject {

    private int newScore;

    public ScoreUpdateEvent(Object source, int newScore) {
        super(source);
        this.newScore = newScore;
    }

    public int getNewScore() {
        return newScore;
    }
}
