package Networking;

import GameComponents.Cell;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


/**
 * Handler for each individual client connected to server
 *
 * @author Andrew
 */
public class ClientHandler implements Runnable {

    public Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ServerService server;
    private String clientName;


    /**
     * Constructor
     * @param client client connected
     * @param server server client is connected to
     * @throws IOException Client suddenly disconnects (shouldn't happen if client exits correctly)
     * @throws ClassNotFoundException Unexpected Exception
     */
    public ClientHandler(Socket client, ServerService server) throws IOException, ClassNotFoundException {
        this.client = client;
        this.server = server;
        out = new ObjectOutputStream(client.getOutputStream());
        in = new ObjectInputStream(client.getInputStream());
        clientName = (String)in.readObject();
    }

    /**
     * Sends object over to client
     * @param obj object to be sent
     */
    public void send(Object obj) {
        try {
            out.writeObject(obj);
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnects client from server
     */
    public void disconnect() {
        send(new ServerConnectionEvent(this, ServerConnectionEvent.EXPECTED_DISCONNECT));
    }

    /**
     * Gets name of client
     * @return name the name of the client
     */
    public String getName() {
        return clientName;
    }

    /**
     * Handles received objects from client
     * @throws IOException Client Disconnects
     * @throws ClassNotFoundException Shouldn't Happen
     */
    public void receive() throws IOException, ClassNotFoundException {
        Object msg = in.readObject();   // Receive Object
        if (msg instanceof ChatEvent) { //Chat Message
            server.sendAll(msg);
        } else if (msg instanceof GameStateEvent) {    // Either Lose or Win
            GameStateEvent event = (GameStateEvent)msg;
            if (event.getType() == GameStateEvent.LOST) {
                server.playerLose(this);
            } else if (event.getType() == GameStateEvent.READY){
                server.playerReady();
            } else if (event.getType() == GameStateEvent.WIN) {
                server.playerWin(this);
            }
        }else if (msg instanceof ArrayList){    // Updated cells
            if (!server.hasStarted()) { // This means the client disconnected before the game started and is sending dead spawn
                server.sendAll(((ArrayList<Cell>) msg).get(0));
            }else {
                ArrayList<Cell> update = (ArrayList<Cell>) msg;
                server.addUpdate(this, update);
            }
        }else if (msg instanceof ServerConnectionEvent) {   // Client disconnects
            throw new IOException();
        } else {
            throw new ClassNotFoundException(msg.getClass().toString());
        }
    }

    /**
     * Listens for client messages and responds accordingly
     */
    @Override
    public void run() {
        try {
            while (true) {
                receive();
            }
        } catch (IOException ignored) {
            // Client Disconnects
        } catch (ClassNotFoundException e) {
            e.printStackTrace();    // Shouldn't happen
        } finally {
            // Cleanup client
            server.removeClient(this);
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();    // impossible to happen i think
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}