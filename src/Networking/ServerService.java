package Networking;

import GameComponents.Cell;
import GameComponents.Grid;


import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Handles client connections and information transferred from clients
 * @author Andrew
 */
public class ServerService implements Runnable {
    private ServerSocket server;
    private int PORT;
    private HashSet<ClientHandler> clients;
    private HashSet<ClientHandler> playersAlive;
    private static final String LOST_MSG = "!LOST";
    private static final String DISCONNECT_MSG = "!DISCONNECT";
    private int maxPlayers;
    private HashMap<ClientHandler, ArrayList<Cell>> updatedCells;
    private final Object playerLock = new Object();
    private int playersReady;

    private int rows;
    private int cols;
    private int foodAmount;

    public ServerService(int rows, int cols, int foodAmount, int port, int maxPlayers) throws IOException {
        this.maxPlayers = maxPlayers;
        PORT = port;
        server = new ServerSocket(PORT);
        clients = new HashSet<ClientHandler>();
        playersAlive = new HashSet<ClientHandler>();
        updatedCells = new HashMap<ClientHandler, ArrayList<Cell>>();
        this.rows = rows;
        this.cols = cols;
        this.foodAmount = foodAmount;
        playersReady = 0;
    }

    public synchronized void losePlayer(ClientHandler player) {
        playersAlive.remove(player);
        updatedCells.remove(player);
    }
    public synchronized void removeClient(ClientHandler client) {
        losePlayer(client);
        clients.remove(client);
    }

    public synchronized void addUpdate(ClientHandler player, ArrayList<Cell> update) {
        playersReady++;
        System.out.println(update.toString());
        updatedCells.put(player, update);
        if (playersReady == playersAlive.size()) {
            playersReady = 0;
            this.notify();
        }
    }

    public synchronized void sendChat(ClientHandler sender, ChatEvent chatEvent) {
        for (ClientHandler client: clients) {
            if (client != sender) {
                client.send(chatEvent);
            }
        }
    }

    public void sendAll(Object obj) {
        for (ClientHandler client: clients) {
            client.send(obj);
        }
    }

    // there's probably a o(n) way to do this but doesn't matter much, size is going to be so small anyways
    public void sendUpdatedCells() {
        for (ClientHandler receiver: playersAlive) {
            ArrayList<Cell> totalUpdate = new ArrayList<Cell>();
            for (Map.Entry<ClientHandler, ArrayList<Cell>> entry: updatedCells.entrySet()) {
                ClientHandler sender = entry.getKey();
                ArrayList<Cell> update = entry.getValue();
                if (sender != receiver) {
                    totalUpdate.addAll(update);
                }
            }
            receiver.send(totalUpdate);
        }
    }

    @Override
    public void run() {
        // Initialize Game
        Grid grid = new Grid(rows, cols);
        ArrayList<Cell> updatedLocations = new ArrayList<Cell>();
        ArrayList<Cell> possibleLocations = new ArrayList<Cell>();
        for (Cell[] row: grid.getGrid()) {
            possibleLocations.addAll(Arrays.asList(row));
        }
        Random rand = new Random(); // Used for food and spawn generation
        // Generate Food
        for (int i = 0; i < foodAmount; i++) {
            Cell food = possibleLocations.remove(rand.nextInt(possibleLocations.size()));
            food.setFood(true);
            updatedLocations.add(food);
        }

        while (clients.size() < maxPlayers) {
            System.out.println("[SERVER] Waiting for connection...");
            Socket client = null;
            try {
                client = server.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("[SERVER] " + client.toString() + " Connected");
            ClientHandler clientThread = null;
            try {
                clientThread = new ClientHandler(client, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            clients.add(clientThread);
            playersAlive.add(clientThread);

            // Upon connection send client info about grid and spawn; also update other clients of this client
            Cell spawn = possibleLocations.remove(rand.nextInt(possibleLocations.size()));
            spawn.setSnake(true);
            clientThread.send(grid);
            clientThread.send(spawn);
            sendAll(spawn);
            new Thread(clientThread).start();   // Not using thread pool, since construction will only occur at start
        }

        System.out.println("Game starting!");
        sendAll(new SpecialEvent(this, SpecialEvent.START));

        while (playersAlive.size() != 0) {
            synchronized (this) {
                try {
                    this.wait();
                    System.out.println("Notified now sending updated cells");
                    sendUpdatedCells();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerService server = new ServerService(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]),Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        new Thread(server).start();
    }
}
