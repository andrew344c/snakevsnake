package Networking;

import java.io.Serializable;
import java.util.EventObject;

public class ChatEvent extends EventObject implements Serializable {

    private String msg;
    private String sender;

    public ChatEvent(Object source, String sender, String msg) {
        super(source);
        this.sender = sender;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public String getSender() {
        return sender;
    }
}
