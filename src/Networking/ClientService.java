package Networking;
import GameComponents.Cell;
import GameComponents.Game;
import GameComponents.Grid;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Connects and transfer information between client and server
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

    private ChatListener chatListener;
    private UpdateListener updateListener;

    private final static String TYPE_CHAT = "[C]";
    private final static String TYPE_SPECIAL = "[S]";
    private final String LOST_SIGNAL = "[S]LOST";

    public ClientService(String ip, int port) {
        IP = ip;
        PORT = port;
        try {
            server = new Socket(IP, PORT);
            out = new ObjectOutputStream(server.getOutputStream());
            in = new ObjectInputStream(server.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        update = null;
    }

    public Object[] initializeGame() {
        Object[] gridAndSnake = new Object[2];
        try {
            gridAndSnake[0] = in.readObject();
            gridAndSnake[1] = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return gridAndSnake;
    }

    public void send(Object obj) {
        try {
            out.writeObject(obj);
            out.reset(); //clear cache, without it deserializes wrong reference of object
        }catch (IOException e) {

        }
    }

    public void lose(ArrayList<Cell> deadCells) {
        send(new SpecialEvent(this, SpecialEvent.LOST));
        send(deadCells);
    }

    public ArrayList<Cell> getUpdate() {
        ArrayList<Cell> temp = update;
        update = null;
        return temp;
    }

    public void setLock(Game lock) {
        game = lock;
    }

    public void setChatListener(ChatListener chatListener) {
        this.chatListener = chatListener;
    }

    public void fireChatEvent(ChatEvent chatEvent) {
        chatListener.chatEventOccurred(chatEvent);
    }

    public void setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public void fireUpdateEvent(Cell cell) {
        updateListener.updateOccurred(cell);
    }

    public void fireSpecialEvent(SpecialEvent specialEvent) {
        updateListener.specialEventOccurred(specialEvent);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof ChatEvent) {
                    fireChatEvent((ChatEvent)obj);
                } else if (obj instanceof Cell) {   // Only happens during initialization phase
                    fireUpdateEvent((Cell)obj);
                } else if (obj instanceof SpecialEvent) {
                    fireSpecialEvent((SpecialEvent)obj);
                } else if (obj instanceof ArrayList) {
                    update = (ArrayList<Cell>)obj;
                    game.updateReady();
                }
            }
        } catch (IOException e) {
            game.endGame("There was an error with the connection to the server");
        } catch (ClassNotFoundException e) {
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