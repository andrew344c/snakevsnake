package Networking;
import GameComponents.Cell;
import GameComponents.Game;
import GameComponents.Grid;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Connects and transfer information between client and server
 * TODO: Error handling when server goes down, etc.
 * @author Andrew
 */
public class ClientService implements Runnable {
    private Socket server;
    public final int PORT;
    public final String IP;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ArrayList<Cell> update;
    private Game game;

    private final static String TYPE_CHAT = "[C]";
    private final static String TYPE_SPECIAL = "[S]";

    private Object lock;

    public ClientService(String ip, int port) {
        IP = ip;
        PORT = port;
        try {
            server = new Socket(IP, PORT);
            in = new ObjectInputStream(server.getInputStream());
            out = new ObjectOutputStream(server.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        update = null;
        initializeGame();
    }

    public Object[] initializeGame() {
        Object[] snakeAndGridAndUpdate = new Object[3];
        try {
            snakeAndGridAndUpdate[0] = (Cell)in.readObject();
            snakeAndGridAndUpdate[1] = (Grid)in.readObject();
            snakeAndGridAndUpdate[2] = (ArrayList<Cell>)in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return snakeAndGridAndUpdate;
    }

    // add in some way to confirm that cell has been sent
    public void send(Object obj) throws IOException {
        out.writeObject(obj);
    }

    public ArrayList<Cell> getUpdate() {
        return update;
    }

    public void setLock(Object lock) {
        this.lock = lock;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();

                if (obj instanceof String) {
                    String msg = ((String)obj).substring(3);
                    String type = ((String)obj).substring(0, 3);
                    if (type.equals(TYPE_SPECIAL)) {
                        if (msg.equals("WIN")) {

                        }
                    }
                }else if (obj instanceof ArrayList) {
                    update = (ArrayList<Cell>)obj;
                    lock.notify();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}