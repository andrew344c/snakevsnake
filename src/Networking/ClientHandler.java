package Networking;

import GameComponents.Cell;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


/**
 * Handler for client
 * TODO: Error handling when socket goes down, etc.
 *
 * @author Andrew
 */
public class ClientHandler implements Runnable {
    private Socket client;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ServerService server;

    private boolean onSpecial;  // When true: looking for special keyword, When false: looking for cell
    private final static String TYPE_CHAT = "[C]";
    private final static String TYPE_SPECIAL = "[S]";

    public ClientHandler(Socket client, ServerService server) throws IOException {
        this.client = client;
        this.server = server;
        in = new ObjectInputStream(client.getInputStream());
        out = new ObjectOutputStream(client.getOutputStream());
    }

    public void send(Object obj) {
        try {
            out.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
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
                Object msg = in.readObject();   //Receive Object
                if (msg instanceof String) {    //Either chat or special keyword

                    String message = ((String)msg).substring(3);
                    String type = ((String)msg).substring(0, 3);

                    if (type.equals(TYPE_SPECIAL)) { //Special Keyword
                        if (message.equals("LOST")) {
                            server.removePlayer(this);
                        }else if (message.equals("CONTINUE")) {
                            //we good
                        }
                    }else if (type.equals(TYPE_CHAT)) { //Chat

                    }
                    System.out.print(client + " ");
                    System.out.println(msg);
                }else if (msg instanceof ArrayList){
                    ArrayList<Cell> update = (ArrayList<Cell>)msg;
                    server.addUpdate(this, update);
                }else if (msg == null){
                    break;
                }else {
                    throw new ClassNotFoundException();
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