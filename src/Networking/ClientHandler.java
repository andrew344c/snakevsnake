package Networking;

import GameComponents.Cell;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


/**
 * Handler for client
 *
 * @author Andrew
 */
public class ClientHandler implements Runnable {
    public Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ServerService server;

    private boolean onSpecial;  // When true: looking for special keyword, When false: looking for cell
    private final static String TYPE_CHAT = "[C]";
    private final static String TYPE_SPECIAL = "[S]";

    public ClientHandler(Socket client, ServerService server) throws IOException {
        this.client = client;
        this.server = server;
        out = new ObjectOutputStream(client.getOutputStream());
        in = new ObjectInputStream(client.getInputStream());
    }

    public void send(Object obj) {
        System.out.println(obj.toString());
        try {
            out.writeObject(obj);
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receive() throws IOException, ClassNotFoundException {
        Object msg = in.readObject();   //Receive Object
        System.out.println("Received: " + msg.toString() + " From: " + client.toString());
        if (msg instanceof ChatEvent) {
            server.sendAll(msg);
        } else if (msg instanceof SpecialEvent) {    //Either chat or special keyword
            SpecialEvent event = (SpecialEvent)msg;
            if (event.getType() == SpecialEvent.LOST) {
                server.losePlayer(this);
            }
        }else if (msg instanceof ArrayList){
            ArrayList<Cell> update = (ArrayList<Cell>)msg;
            server.addUpdate(this, update);
        }else {
            throw new ClassNotFoundException();
        }
    }

    /**
     * Listen for client
     */
    @Override
    //Client sending procedure: Send special keyword (signified by "[S]" in beginning) then send Cells
    public void run() {
        try {
            while (true) {
                receive();
            }
        } catch (IOException ignored) {

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
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