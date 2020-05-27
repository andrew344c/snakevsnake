package Networking;

import java.io.Serializable;
import java.util.EventObject;

public class ServerConnectionEvent extends EventObject implements Serializable {

    public final static int EXPECTED_DISCONNECT = 0;
    public final static int UNEXPECTED_DISCONNECT = 1;
    public final static int UNSUCCESSFUL_CONNECTION_ATTEMPT = 2;

    private int type;

    public ServerConnectionEvent(Object source, int reason) {
        super(source);
        this.type = reason;
    }

    public int getType() {
        return type;
    }
}
