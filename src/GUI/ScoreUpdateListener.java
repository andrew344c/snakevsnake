package GUI;

import java.util.EventListener;

/**
 * Acts as direct communication for Game to GameWindow
 * Used for updating score
 *
 * @author Andrew
 */
public interface ScoreUpdateListener extends EventListener {
    public void scoreUpdateEventOccurred(ScoreUpdateEvent event);
    public void updateGoalScore(ScoreUpdateEvent event);
}
