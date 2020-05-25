package GUI;

import java.util.EventObject;

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
