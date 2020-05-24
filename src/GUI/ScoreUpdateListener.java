package GUI;

import java.util.EventListener;

public interface ScoreUpdateListener extends EventListener {
    public void scoreUpdateEventOccurred(ScoreUpdateEvent event);
}
