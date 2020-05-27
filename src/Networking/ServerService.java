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
    private HashSet<ClientHandler> clients;
    private HashSet<String> playersAlive;
    private int amountPlayers;
    private HashMap<ClientHandler, ArrayList<Cell>> updatedCells;
    private int playersReady;
    private ArrayList<Cell> deadCells;
    private ArrayList<String> playersWon;
    private ArrayList<String> playersLost;
    private boolean started;

    private int rows;
    private int cols;
    private int foodAmount;
    private int goal;
    private boolean autoDisconnect;

    /**
     * ServerService Constructor
     * @param rows the rows for the grid
     * @param cols the cols for the grid
     * @param foodAmount the amount of food to be generated
     * @param port the port the server is hosted on
     * @param amountPlayers the maximum amount of players allowed
     * @throws IOException Error while creating server
     */
    public ServerService(int rows, int cols, int foodAmount, int amountPlayers, int goal, int port, boolean autoDisconnect) throws IOException {
        this.amountPlayers = amountPlayers;
        this.rows = rows;
        this.cols = cols;
        this.foodAmount = foodAmount;
        this.goal = goal;
        this.autoDisconnect = autoDisconnect;
        server = new ServerSocket(port);
        clients = new HashSet<ClientHandler>();
        updatedCells = new HashMap<ClientHandler, ArrayList<Cell>>();
        playersAlive = new HashSet<String>();
        playersReady = 0;
        deadCells = new ArrayList<Cell>();
        playersWon = new ArrayList<String>();
        playersLost = new ArrayList<String>();
        started = false;
    }

    /**
     * Player wins
     * @param player wins the game
     */
    public synchronized void playerWin(ClientHandler player) {
        playersWon.add(player.getName());
    }

    /**
     * PLayer loses
     * @param player loses the game
     */
    public synchronized void playerLose(ClientHandler player) {
        playersLost.add(player.getName());
        deadCells.addAll(updatedCells.get(player));
        updatedCells.remove(player);
        playersAlive.remove(player.getName());
    }

    /**
     * A player is ready to receive updates and has already sent their updated cells
     */
    public synchronized void playerReady() {
        playersReady++;
        if (playersReady == playersAlive.size()) {
            playersReady = 0;
            this.notify();
        }
    }

    /**
     * Removes client
     * @param client to be removed
     */
    public synchronized void removeClient(ClientHandler client) {
        if (started) {
            playerLose(client);
        }
        clients.remove(client);
    }

    /**
     * Adds updated cells from a client
     * @param player player that updated cells
     * @param update the updated cells that they sent over
     */
    public synchronized void addUpdate(ClientHandler player, ArrayList<Cell> update) {
        updatedCells.put(player, update);
    }

    /**
     * Send to all clients
     * @param obj to be sent
     */
    public synchronized void sendAll(Object obj) {
        for (ClientHandler client: clients) {
            client.send(obj);
        }
    }

    // there's probably a o(n) way to do this but doesn't matter much, size is going to be so small anyways
    /**
     * Sends all updated cells received from players to every other player
     */
    public void sendUpdatedCells() {
        // Iterate through players alive and send them updated cells from other players, but not cells from themselves
        for (ClientHandler receiver: clients) {
            ArrayList<Cell> totalUpdate = new ArrayList<Cell>(); // Update to be sent to receiver
            for (Map.Entry<ClientHandler, ArrayList<Cell>> entry: updatedCells.entrySet()) {
                ClientHandler sender = entry.getKey();
                ArrayList<Cell> update = entry.getValue();
                if (sender != receiver) {   // Don't send own cells back
                    totalUpdate.addAll(update);
                }
            }
            totalUpdate.addAll(deadCells); // Add all cells of players that lost
            receiver.send(totalUpdate);
        }
    }

    public boolean hasStarted() {
        return started;
    }

    /**
     * Listen for clients till the amount of players is reached then starts the game
     * During game, wait for every player to send their updated cells and then send those updated cells to all other players
     * At end of game doesn't auto shut down server (unless autoDisconnect is set to true) to allow players to chat
     */
    @Override
    public void run() {
        // Initialize Game
        Grid grid = new Grid(rows, cols);
        ArrayList<Cell> possibleLocations = new ArrayList<Cell>();
        for (Cell[] row: grid.getGrid()) {
            possibleLocations.addAll(Arrays.asList(row));
        }
        Random rand = new Random(); // Used for food and spawn generation
        // Generate Food
        for (int i = 0; i < foodAmount; i++) {
            Cell food = possibleLocations.remove(rand.nextInt(possibleLocations.size()));
            food.setFood(true);
        }

        // Listen for new client connections and handle appropriately
        while (clients.size() < amountPlayers) {
            System.out.println("Waiting for connection...");
            Socket client = null;
            try {
                client = server.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(client.toString() + " Connected");
            ClientHandler clientThread = null;
            try {
                clientThread = new ClientHandler(client, this);
            } catch (IOException e) {
                continue;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            clients.add(clientThread);
            playersAlive.add(clientThread.getName());

            // Upon connection send client info about grid and spawn and required score; also update other clients of this client
            Cell spawn = possibleLocations.remove(rand.nextInt(possibleLocations.size()));
            spawn.setSnake(true);
            clientThread.send(grid);
            clientThread.send(spawn);
            clientThread.send(goal);
            sendAll(spawn);
            new Thread(clientThread).start();   // Not using thread pool, since construction will only occur at start
        }

        System.out.println("Game starting!");
        sendAll(new GameStateEvent(this, GameStateEvent.START));
        started = true;

        while (true) {
            synchronized (this) {
                try {
                    this.wait();

                    sendUpdatedCells();

                    if (playersAlive.size() == 0) {    // Last snakes bumped into each other
                        GameStateEvent tieEvent = new GameStateEvent(this, GameStateEvent.TIE);
                        tieEvent.setPlayers(playersLost);
                        sendAll(tieEvent);
                        break;
                    } else if (playersWon.size() > 1) { // Multiple players won
                        GameStateEvent tieEvent = new GameStateEvent(this, GameStateEvent.TIE);
                        tieEvent.setPlayers(playersWon);
                        sendAll(tieEvent);
                        break;
                    } else if (playersWon.size() == 1) {    //One player wins
                        GameStateEvent winEvent = new GameStateEvent(this, GameStateEvent.WIN);
                        winEvent.setPlayers(playersWon);
                        sendAll(winEvent);
                        break;
                    } else if (playersAlive.size() == 1) { // One player remaining
                        GameStateEvent winEvent = new GameStateEvent(this, GameStateEvent.WIN);
                        winEvent.setPlayers(new ArrayList<String>(playersAlive));
                        break;
                    }

                    playersWon = new ArrayList<String>();
                    playersLost = new ArrayList<String>();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (autoDisconnect) {
            disconnectAll();
            System.exit(0);
        }
    }

    /**
     * Disconnects all clients connected
     */
    public void disconnectAll() {
        for (ClientHandler client: clients) {
            client.disconnect();
        }
    }

    /**
     * Main
     * @param args no command line args are used
     * @throws IOException Error in forming server
     */
    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);
        System.out.print("Rows: ");
        int rows = scan.nextInt();
        System.out.print("Cols: ");
        int cols = scan.nextInt();
        System.out.print("Amount of Food: ");
        int food = scan.nextInt();
        System.out.print("Amount of players: ");
        int players = scan.nextInt();
        System.out.print("Goal: ");
        int goal = scan.nextInt();
        System.out.print("Port to be hosted on: ");
        int port = scan.nextInt();
        System.out.print("Automatically shut down server after game? (y or n)");
        boolean autoDisconnect = scan.next().equalsIgnoreCase("y");
        System.out.println("Note: To shut down the server at any time, enter 'stop' in stdin");

        ServerService server = new ServerService(rows, cols, food, players, goal, port, autoDisconnect);
        Thread serverThread = new Thread(server);
        serverThread.start();
        if (scan.next().equalsIgnoreCase("stop")) {
            server.disconnectAll();
            System.exit(0);
        }
    }
}
