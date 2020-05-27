package Networking;
import GameComponents.Cell;
import GameComponents.Game;


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
    private ServerListener serverListener;
    private ServerConnectionListener serverConnectionListener;

    private boolean spectateMode;

    public ClientService(String ip, int port, String name) {
        spectateMode = false;
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
        try {
            out.writeObject(name);
            out.reset();
        } catch (IOException e) {

        }
    }

    public Object[] initializeGame() {
        // The format will be [grid, snake spawn location (cell), goal score (int)]
        // These will always be sent upon client's connection to the server
        Object[] initializationData = new Object[3];
        try {
            initializationData[0] = in.readObject();
            initializationData[1] = in.readObject();
            initializationData[2] = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return initializationData;
    }

    /**
     * Sends object over to server
     * (Made synchronized since is used for Game Updates and also Chat, so there's possibility of accessing at same time)
     * @param obj
     */
    public synchronized void sendUpdate(Object obj) {
        try {
            out.writeObject(obj);
            out.writeObject(new GameStateEvent(this, GameStateEvent.READY));
            out.reset(); //clear cache, without it deserializes wrong reference of object later on
        }catch (IOException e) {

        }
    }

    public synchronized void sendChat(ChatEvent chat) {
        try {
            out.writeObject(chat);  // This chat event reference will not be reused, no need to reset cache
        }catch (IOException e) {

        }
    }

    /**
     * Called when player loses, sends dead cells of snake and lost event
     * (Made synchronized since if user abruptly disconnects and loses at the same time, this method may be called at same time)
     * @param deadCells
     */
    public synchronized void lose(ArrayList<Cell> deadCells) {
        try {
            out.writeObject(deadCells);
            out.writeObject(new GameStateEvent(this, GameStateEvent.LOST));
            out.writeObject(new GameStateEvent(this, GameStateEvent.READY));
            out.reset();
        } catch (IOException e) {

        }
    }

    public synchronized void win() {
        try {
            out.writeObject(new GameStateEvent(this, GameStateEvent.WIN));
            // No need to reset here
        } catch (IOException e) {

        }
    }

    public void disconnect(ArrayList<Cell> deadCells) {
        try {
            out.writeObject(deadCells);
            out.writeObject(new ServerConnectionEvent(this, ServerConnectionEvent.EXPECTED_DISCONNECT));
            out.reset();
        }catch (IOException e) {

        }
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

    public void setServerConnectionListener(ServerConnectionListener serverConnectionListener) {
        this.serverConnectionListener = serverConnectionListener;
    }

    public void fireChatEvent(ChatEvent chatEvent) {
        chatListener.chatEventOccurred(chatEvent);
    }

    public void setServerListener(ServerListener serverListener) {
        this.serverListener = serverListener;
    }

    public void fireUpdateEvent(Cell cell) {
        serverListener.updateOccurred(cell);
    }

    public void fireUpdateEvent(ArrayList<Cell> cells) {
        serverListener.updateOccurred(cells);
    }

    public void fireSpecialEvent(GameStateEvent gameStateEvent) {
        serverListener.gameStateEventOccurred(gameStateEvent);
    }

    public void spectateMode() {
        spectateMode = true;
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
                } else if (obj instanceof GameStateEvent) {
                    fireSpecialEvent((GameStateEvent)obj);
                } else if (obj instanceof ArrayList) {
                    if (spectateMode) { // During "spectate" mode, just directly pass update to GUI
                        fireUpdateEvent((ArrayList<Cell>)obj);
                    }else { // Otherwise, call updateReady for Game to handle update
                        update = (ArrayList<Cell>) obj;
                        game.updateReady();
                    }
                }
            }
        } catch (IOException e) {

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