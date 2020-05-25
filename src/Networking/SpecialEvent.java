package Networking;

import java.io.Serializable;
import java.util.EventObject;

public class SpecialEvent extends EventObject implements Serializable {

    private int type;

    public static final int START = 0;
    public static final int LOST = 1;
    public static final int WIN = 2;

    public SpecialEvent(Object source, int type) {
        super(source);
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
