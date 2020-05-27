package GameComponents;

import GUI.ScoreUpdateEvent;
import GUI.ScoreUpdateListener;
import Networking.*;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Main handler for game
 *
 * @author Andrew
 */
public class Game {
    private Grid grid;
    private Snake player;
    private ClientService server;
    private Thread serverThread;
    private ArrayList<Cell> updatedCells;
    private boolean lost;   // Only used in single player

    private ScoreUpdateListener scoreUpdateListener;
    private int goal; // Only used in multi player

    /**
     * Single-player constructor
     *
     * @param rows
     * @param cols
     */
    public Game(int rows, int cols) {
        //Initialize grid, snake, and updated cells
        grid = new Grid(rows, cols);
        player = new Snake(grid.at(cols / 2, rows / 2), grid);
        updatedCells = new ArrayList<Cell>();
        lost = false;

        //Update snake's initial position
        try {
            player.update();
        } catch (SnakeOutOfBoundsException ignored) {}
        player.getHead().setSnake(true);
        updatedCells.add(player.getHead());
        updatedCells.add(grid.generateFood());
    }

    /**
     * Multi-player constructor
     *
     * @param ip    ip of server
     * @param port  port of server
     * @param name  name to be displayed
     */
    public Game(String ip, int port, String name) {
        lost = false;
        updatedCells = new ArrayList<Cell>();

        //Setup connection to server
        server = new ClientService(ip, port, name);
        server.setLock(this);   // Used for blocking the game while waiting for updates

        // The format will be [grid, snake spawn location (cell), goal score (int)]
        Object[] initializationUpdates = server.initializeGame();
        grid = (Grid)initializationUpdates[0];
        Cell start = (Cell)initializationUpdates[1];
        player = new Snake((grid.at(start.getX(), start.getY())), grid);
        goal = (int)initializationUpdates[2];

        for (Cell[] row: grid.getGrid()) {
            updatedCells.addAll(Arrays.asList(row));
        }
    }

    /**
     * Start continuously listening for server messages
     */
    public void startListening() {
        serverThread = new Thread(server);
        serverThread.start();
    }

    /**
     * Used in GamePanel: gives the updates
     * @return updated cells
     */
    public ArrayList<Cell> getUpdates() {
        ArrayList<Cell> temp = updatedCells;
        updatedCells = new ArrayList<Cell>();
        return temp;
    }

    /**
     * Called when key is pressed
     * @param e key-press
     */
    public void keyPressed(KeyEvent e) {
        player.keyPressed(e);
    }


    /**
     * Resembles a "tick" in the game
     * Moves snake, updates cell, sends updates to server, receives updates from server during this tick
     * @throws InterruptedException only used in single player: resembles end of single-player game
     */
    public void update() throws InterruptedException {
        // Update snake body (Will also update tail in grid directly in update call, but head will not be updated in grid)
        try {
            updatedCells = player.update(); //Will return all cells that need to be updated
        }catch (SnakeOutOfBoundsException error) {
            updatedCells = player.killSnake();
            lost = true;
        }

        // Sends and updates cell info to/from server
        if (server != null) {
            if (lost) {
                server.lose(updatedCells); // Updated Cells is updated in killSnake call
            }else {
                //sending "projection" of head and then changing back
                //Cannot directly move head, since we need the updates from server first to see if the move is legal or not
                boolean previous = player.getHead().hasSnake();
                player.getHead().setSnake(true);
                server.sendUpdate(updatedCells);
                player.getHead().setSnake(previous);
            }

            //Wait for and update from server
            acceptUpdate();
        }

        // Snake Collision and Food generation: if client eats food, is responsible for generating new food and sends to server
        Cell food = null;
        if (!lost) {
            if (player.isMoving()) {
                if (!player.getHead().hasSnake()) { // Not colliding into snake
                    player.getHead().setSnake(true);    // If no snake in front, approve snake head's location other wise send lost signal
                    if (player.ate) {
                        food = grid.generateFood();
                        player.ate = false;
                        updatedCells.add(food);
                        fireScoreUpdateEvent(new ScoreUpdateEvent(this, player.getScore()));
                        if (server != null && player.getScore() == goal) {
                            server.win();
                        }
                    }
                } else {    // Bumped into snake
                    updatedCells = player.killSnake();
                    lost = true;
                    if (server != null) {
                        server.lose(updatedCells);
                    }
                }
            }
        }

        // Send food update to server and also receive updates from server (these update could be dead snakes and food)
        if (server != null && !lost) {
            // Send food
            if (food != null) {
                server.sendUpdate(new ArrayList<Cell>(Collections.singletonList(food)));
            }else {
                server.sendUpdate(new ArrayList<>());
            }
        }

        // Wait for and update from server
        if (server != null) {
            acceptUpdate();
        }

        if (lost && server != null) {
            throw new InterruptedException();
        }
    }

    /**
     * Gets rows
     * @return rows of grid
     */
    public int getRows() {
        return grid.getRows();
    }

    /**
     * Get cols
     * @return cols of grid
     */
    public int getCols() {
        return grid.getCols();
    }

    /**
     * Used in ClientService
     * Notifies game that update is ready and to stop waiting
     */
    public synchronized void updateReady() {
        this.notify();
    }

    /**
     * Waits for and Accepts an update from ClientService
     * @throws InterruptedException interruption in thread waiting
     */
    public void acceptUpdate() throws InterruptedException{
        // Wait for update from server
        ArrayList<Cell> update = server.getUpdate();
        if (update == null) {
            synchronized (this) {
                this.wait();
                update = server.getUpdate();
            }
        }

        // Add update to server
        for (Cell cell: update) {
            updatedCells.add(cell);
            grid.updateCell(cell);
        }
    }

    /**
     * Sets score listener
     * @param scoreUpdateListener listens for updates in the score and sends to GUI
     */
    public void setScoreUpdateListener(ScoreUpdateListener scoreUpdateListener) {
        this.scoreUpdateListener = scoreUpdateListener;
        scoreUpdateListener.updateGoalScore(new ScoreUpdateEvent(this, goal));
    }

    /**
     * Fires score update event
     * @param event a update in the score
     */
    public void fireScoreUpdateEvent(ScoreUpdateEvent event) {
        scoreUpdateListener.scoreUpdateEventOccurred(event);
    }

    /**
     * Merely passes along listener from GUI to ClientService
     * @param chatListener listener for chat
     */
    public void setChatListener(ChatListener chatListener) {
        server.setChatListener(chatListener);
    }

    /**
     * Merely passes along listener from GUI to ClientService
     * @param serverListener listens for server updates
     */
    public void setUpdateListener(ServerListener serverListener) {
        server.setServerListener(serverListener);
    }

    /**
     * Merely passes along listener from GUI to ClientService
     * @param serverConnectionListener listens for changes in connection
     */
    public void setServerConnectionListener(ServerConnectionListener serverConnectionListener) {
        server.setServerConnectionListener(serverConnectionListener);
    }

    /**
     * Send chat over to server
     * @param event chat to be sent
     */
    public void sendChat(ChatEvent event) {
        server.sendChat(event);
    }

    /**
     * Disconnect from server
     */
    public void disconnect() {
        server.disconnect(player.killSnake());
    }
    /**
     * Called when game finishes, ends the game loop
     */
    public void spectateMode() {
        server.spectateMode();
    }
}
