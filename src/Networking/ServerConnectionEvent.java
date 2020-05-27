package Networking;

import java.io.Serializable;
import java.util.EventObject;

/**
 * A event representing a change in the connection between the server and client
 * This change may be intentional or unexpected
 */
public class ServerConnectionEvent extends EventObject implements Serializable {

    public final static int EXPECTED_DISCONNECT = 0;
    public final static int UNEXPECTED_DISCONNECT = 1;
    public final static int UNSUCCESSFUL_CONNECTION_ATTEMPT = 2;

    private int type;

    /**
     * Constructor
     * @param source called of event
     * @param reason type of change in connection
     */
    public ServerConnectionEvent(Object source, int reason) {
        super(source);
        this.type = reason;
    }

    /**
     * Gets type
     * @return type the type or reason for the change in connection
     */
    public int getType() {
        return type;
    }
}
