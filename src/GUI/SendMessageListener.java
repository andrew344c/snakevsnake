package GUI;

import java.util.EventListener;

/**
 * Listens for sending of messages
 */
public interface SendMessageListener extends EventListener {
    /**
     * Message is sent
     * @param event message being sent
     */
    public void sendMessageEventOccurred(SendMessageEvent event);
}
