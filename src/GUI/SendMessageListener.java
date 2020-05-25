package GUI;

import java.util.EventListener;

public interface SendMessageListener extends EventListener {
    public void sendMessageEventOccurred(SendMessageEvent event);
}
