package Networking;
import GameComponents.Cell;
import GameComponents.Game;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Connects and transfers information between client and server
 * @author Andrew
 */
public class ClientService implements Runnable {
    // Server-Client Connection Related Components
    private Socket server;
    public final int PORT;
    public final String IP;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // Game Related Components
    private ArrayList<Cell> update;
    private Game game;  // Only used to lock game
    private boolean spectateMode;

    // Listeners (Direct communication to GUIs)
    private ChatListener chatListener;
    private ServerListener serverListener;
    private ServerConnectionListener serverConnectionListener;

    private ServerConnectionEvent connectionEventQueuedToBeFired;   // Used since ServerConnectionEvent can occur before setting of listener

    /**
     * Constructor
     * @param ip ip of server
     * @param port port of server
     * @param name name to be displayed
     */
    public ClientService(String ip, int port, String name) {
        spectateMode = false;
        IP = ip;
        PORT = port;
        try {
            server = new Socket(IP, PORT);
            out = new ObjectOutputStream(server.getOutputStream());
            in = new ObjectInputStream(server.getInputStream());
        } catch (IOException e) {
            fireServerConnectionEvent(new ServerConnectionEvent(this, ServerConnectionEvent.UNSUCCESSFUL_CONNECTION_ATTEMPT));
            return;
        }
        update = null;
        try {
            out.writeObject(name);
            out.reset();
        } catch (IOException e) {
            fireServerConnectionEvent(new ServerConnectionEvent(this, ServerConnectionEvent.UNEXPECTED_DISCONNECT));
        }
    }

    /**
     * Receives initializing information for Game to set up
     *
     * The format returned will be [grid, snake spawn location (cell), goal score (int)]
     * These will always be sent upon client's connection to the server
     * @return initialization data
     */
    public Object[] initializeGame() {
        Object[] initializationData = new Object[3];
        try {
            initializationData[0] = in.readObject();
            initializationData[1] = in.readObject();
            initializationData[2] = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            fireServerConnectionEvent(new ServerConnectionEvent(this, ServerConnectionEvent.UNEXPECTED_DISCONNECT));
        }
        return initializationData;
    }

    /**
     * Sends object over to server
     * (Made synchronized since is used for Game Updates and also Chat, so there's possibility of accessing at same time)
     * @param obj obj to be sent
     */
    public synchronized void sendUpdate(ArrayList<Cell> obj) {
        try {
            out.writeObject(obj);
            out.writeObject(new GameStateEvent(this, GameStateEvent.READY));
            out.reset(); //clear cache, without it deserializes wrong reference of object later on
        }catch (IOException e) {
            fireServerConnectionEvent(new ServerConnectionEvent(this, ServerConnectionEvent.UNEXPECTED_DISCONNECT));
        }
    }

    /**
     * Sends chat over to server
     * @param chat ChatEvent that holds message of chat sent
     */
    public synchronized void sendChat(ChatEvent chat) {
        try {
            out.writeObject(chat);  // This chat event reference will not be reused, no need to reset cache
        }catch (IOException e) {
            fireServerConnectionEvent(new ServerConnectionEvent(this, ServerConnectionEvent.UNEXPECTED_DISCONNECT));
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
            fireServerConnectionEvent(new ServerConnectionEvent(this, ServerConnectionEvent.UNEXPECTED_DISCONNECT));
        }
    }

    /**
     * Called when player wins, sends win event
     */
    public synchronized void win() {
        try {
            out.writeObject(new GameStateEvent(this, GameStateEvent.WIN));
            // No need to reset here
        } catch (IOException e) {
            fireServerConnectionEvent(new ServerConnectionEvent(this, ServerConnectionEvent.UNEXPECTED_DISCONNECT));
        }
    }

    /**
     * Called when user disconnects
     * @param deadCells cells of the now dead snake
     */
    public void disconnect(ArrayList<Cell> deadCells) {
        try {
            out.writeObject(deadCells);
            out.writeObject(new ServerConnectionEvent(this, ServerConnectionEvent.EXPECTED_DISCONNECT));
            out.reset();
        }catch (IOException e) {
            fireServerConnectionEvent(new ServerConnectionEvent(this, ServerConnectionEvent.UNEXPECTED_DISCONNECT));
        }
    }

    /**
     * Get the updated cells that was received from the server and then set them to null
     * @return update the updated cells from the server
     */
    public ArrayList<Cell> getUpdate() {
        ArrayList<Cell> temp = update;
        update = null;
        return temp;
    }

    /**
     * Sets the lock used for when client is waiting for update
     * Used in unblocking game, when update is finally received
     * @param lock
     */
    public void setLock(Game lock) {
        game = lock;
    }

    /**
     * Sets ChatListener
     * @param chatListener Listens for chat messages from server and then sends over directly to GUI
     */
    public void setChatListener(ChatListener chatListener) {
        this.chatListener = chatListener;
    }

    /**
     * Set serverConnectionListener
     * There may be a connected event queued up already before this being set, thus if so, fire that event
     * @param serverConnectionListener Listens for disruptions in the server connection and sends over directly to GUI to directly respond
     */
    public void setServerConnectionListener(ServerConnectionListener serverConnectionListener) {
        this.serverConnectionListener = serverConnectionListener;
        if (connectionEventQueuedToBeFired != null) {
            fireServerConnectionEvent(connectionEventQueuedToBeFired);
        }
    }

    /**
     * Sets serverListener
     * @param serverListener Listens for updates in the server (these updates can be cell updates (only used in
     *                       waiting phase and spectate phase, before game started) or GameState events
     */
    public void setServerListener(ServerListener serverListener) {
        this.serverListener = serverListener;
    }

    /**
     * Fires chat event
     * @param chatEvent a chat message from another client
     */
    public void fireChatEvent(ChatEvent chatEvent) {
        chatListener.chatEventOccurred(chatEvent);
    }

    /**
     * Fires update event (only used in waiting phase and spectate mode)
     * @param cell updated cell
     */
    public void fireUpdateEvent(Cell cell) {
        serverListener.updateOccurred(cell);
    }

    /**
     * Fires update event
     * @param cells updated cells
     */
    public void fireUpdateEvent(ArrayList<Cell> cells) {
        serverListener.updateOccurred(cells);
    }

    /**
     * Fire game state event
     * @param gameStateEvent a change in game state (win, lose, tie)
     */
    public void fireGameStateEvent(GameStateEvent gameStateEvent) {
        serverListener.gameStateEventOccurred(gameStateEvent);
    }

    /**
     * Fires server connection event
     * @param serverConnectionEvent a disrupt in the connection with the server
     */
    public void fireServerConnectionEvent(ServerConnectionEvent serverConnectionEvent) {
        if (serverConnectionListener != null) {
            serverConnectionListener.connectionEventOccurred(serverConnectionEvent);
        }else {
            connectionEventQueuedToBeFired = serverConnectionEvent;
        }
    }

    /**
     * Used after players loses, now directly receives cells
     */
    public void spectateMode() {
        spectateMode = true;
    }

    /**
     * Main run method for client
     * Listens for objects from server and handles appropriately
     */
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
                    fireGameStateEvent((GameStateEvent)obj);
                } else if (obj instanceof ArrayList) {
                    if (spectateMode) { // During "spectate" mode, just directly pass update to GUI
                        fireUpdateEvent((ArrayList<Cell>)obj);
                    }else { // Otherwise, call updateReady for Game to handle update
                        update = (ArrayList<Cell>) obj;
                        game.updateReady();
                    }
                } else if (obj instanceof ServerConnectionEvent) {  // Server disconnects this client
                    fireServerConnectionEvent((ServerConnectionEvent)obj);
                    break;
                }
            }
        } catch (IOException e) {
            fireServerConnectionEvent(new ServerConnectionEvent(this, ServerConnectionEvent.UNEXPECTED_DISCONNECT));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            // Close connection
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