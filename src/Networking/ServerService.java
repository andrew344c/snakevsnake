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
    private ArrayList<ClientHandler> clients;
    private HashSet<ClientHandler> playersAlive;
    private static final String LOST_MSG = "!LOST";
    private static final String DISCONNECT_MSG = "!DISCONNECT";
    private int maxPlayers;
    private HashMap<ClientHandler, ArrayList<Cell>> updatedCells;
    private final Object playerLock = new Object();

    private int rows;
    private int cols;
    private int foodAmount;

    public ServerService(int rows, int cols, int foodAmount, int port, int maxPlayers) throws IOException {
        this.maxPlayers = maxPlayers;
        PORT = port;
        server = new ServerSocket(PORT);
        clients = new ArrayList<ClientHandler>();
        playersAlive = new HashSet<ClientHandler>();
        updatedCells = new HashMap<ClientHandler, ArrayList<Cell>>();
        this.rows = rows;
        this.cols = cols;
        this.foodAmount = foodAmount;
    }

    //insanely stupid but good enough also i might just move all snake collision and food generation and stuff server side
    //feeling really lazy so change this later
    public void initializeGame() {
        Grid grid = new Grid(rows, cols);
        Random rand = new Random();
        ArrayList<Cell> updatedLocations = new ArrayList<Cell>();

        for (ClientHandler player: playersAlive) {
            Cell possibleSpawn;
            while (true) {
                possibleSpawn = grid.at(rand.nextInt(cols), rand.nextInt(rows));
                if (!possibleSpawn.hasSnake()) {
                    possibleSpawn.setSnake(true);
                    break;
                }
            }
            updatedLocations.add(possibleSpawn);
            player.send(possibleSpawn); //this will be that player snake's starting position
        }

        for (int i = 0; i < foodAmount; i++) {
            Cell possibleSpawn;
            while (true) {
                possibleSpawn = grid.at(rand.nextInt(cols), rand.nextInt(rows));
                if (!possibleSpawn.hasSnake() && !possibleSpawn.hasFood()) {
                    possibleSpawn.setFood(true);
                    break;
                }
            }
            updatedLocations.add(possibleSpawn);
        }

        for (ClientHandler player: playersAlive) {
            player.send(grid);
            player.send(updatedLocations);
        }
    }

    public synchronized void removePlayer(ClientHandler player) {
        playersAlive.remove(player);
    }

    public synchronized void addUpdate(ClientHandler player, ArrayList<Cell> update) {
        System.out.println(update.toString());
        updatedCells.put(player, update);
        if (updatedCells.size() == playersAlive.size()) {
            this.notify();
        }
    }

    public void sendUpdatedCells() {
        if (playersAlive.size() == 1) {
            for (ClientHandler receiver: playersAlive) {
                receiver.send(new ArrayList<Cell>());
            }
        }
        for (ClientHandler receiver: playersAlive) {
            for (Map.Entry<ClientHandler, ArrayList<Cell>> entry: updatedCells.entrySet()) {
                ClientHandler sender = entry.getKey();
                ArrayList<Cell> update = entry.getValue();
                if (sender != receiver) {
                    System.out.println(update.toString());
                    System.out.println(receiver.client.toString());
                    receiver.send(update);
                }
            }
        }
    }

    @Override
    public void run() {
        while (clients.size() < maxPlayers) {
            System.out.println("start");
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
            System.out.println("handling!");
            clients.add(clientThread);
            playersAlive.add(clientThread);
            System.out.println("starting thread");
            new Thread(clientThread).start();   // Not using thread pool, since construction will only occur at start
            System.out.println("end");
            System.out.println(clients.size());
        }

        System.out.println("starting");

        initializeGame();

        System.out.println("done");

        while (playersAlive.size() != 0) {
            synchronized (this) {
                try {
                    this.wait();
                    System.out.println("notified now sending updated cells");
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
