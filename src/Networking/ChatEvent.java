package Networking;

import java.io.Serializable;
import java.util.EventObject;

/**
 * A simple event representing a chat message from another client
 */
public class ChatEvent extends EventObject implements Serializable {

    private String msg;
    private String sender;

    /**
     * Constructor
     * @param source source of event
     * @param sender person who sent message
     * @param msg   message being sent
     */
    public ChatEvent(Object source, String sender, String msg) {
        super(source);
        this.sender = sender;
        this.msg = msg;
    }

    /**
     * Gets message
     * @return msg sent
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Gets sender
     * @return sender sender of msg
     */
    public String getSender() {
        return sender;
    }
}
