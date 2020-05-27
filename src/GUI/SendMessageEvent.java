package GUI;

import java.util.EventObject;

/**
 * Used when user sends a message
 */
public class SendMessageEvent extends EventObject {

    private String sentMsg;

    public SendMessageEvent(Object source, String sentMsg) {
        super(source);
        this.sentMsg = sentMsg;
    }

    public String getSentMsg() {
        return sentMsg;
    }
}
