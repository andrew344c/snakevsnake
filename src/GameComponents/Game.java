package GameComponents;

import GUI.GamePanel;
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
    private boolean lost;
    private GamePanel guiPanel;

    private ScoreUpdateListener scoreUpdateListener;

    /**
     * Single-player constructor
     *
     * @param rows
     * @param cols
     */
    public Game(int rows, int cols, GamePanel panel) {
        guiPanel = panel;
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

    public Game(String ip, int port, GamePanel panel) {
        guiPanel = panel;
        lost = false;
        updatedCells = new ArrayList<Cell>();

        //Setup connection to server
        server = new ClientService(ip, port);
        server.setLock(this);

        Object[] gridAndSnake = server.initializeGame();
        grid = (Grid)gridAndSnake[0];
        Cell start = (Cell)gridAndSnake[1];
        player = new Snake((grid.at(start.getX(), start.getY())), grid);

        for (Cell[] row: grid.getGrid()) {
            updatedCells.addAll(Arrays.asList(row));
        }
    }

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

    public void keyPressed(KeyEvent e) {
        player.keyPressed(e);
    }

    //idk what happened code got really messy, fix it up later
    public void update() throws InterruptedException, SocketInexistentException {
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
                //sketchy dush
                //sending "projection" of head and then changing back
                boolean previous = player.getHead().hasSnake();
                player.getHead().setSnake(true);
                server.send(updatedCells);
                player.getHead().setSnake(previous);
            }

            //Wait for and update from server
            acceptUpdate();
        }

        // Food generation: if client eats food, is responsible for generating new food and sends to server
        Cell food = null;
        if (!lost) {
            // Food generation and snake collision
            if (player.isMoving()) {
                if (!player.getHead().hasSnake()) { // Not colliding into snake
                    player.getHead().setSnake(true);    // If no snake in front, approve snake head's location other wise send lost signal
                    if (player.ate) {
                        food = grid.generateFood();
                        player.ate = false;
                        updatedCells.add(food);
                        fireScoreUpdateEvent(new ScoreUpdateEvent(this, player.getScore()));
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
                server.send(new ArrayList<Cell>(Collections.singletonList(food)));
            }else {
                server.send(new ArrayList<>());
            }

            // Wait for and update from server
            acceptUpdate();
        }

        if (lost) {
            throw new InterruptedException();
        }
    }

    public int getRows() {
        return grid.getRows();
    }

    public int getCols() {
        return grid.getCols();
    }

    public synchronized void updateReady() {
        this.notify();
    }

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

    public void setScoreUpdateListener(ScoreUpdateListener scoreUpdateListener) {
        this.scoreUpdateListener = scoreUpdateListener;
    }

    public void fireScoreUpdateEvent(ScoreUpdateEvent event) {
        scoreUpdateListener.scoreUpdateEventOccurred(event);
    }

    public void setChatListener(ChatListener chatListener) {
        server.setChatListener(chatListener);
    }

    public void setUpdateListener(UpdateListener updateListener) {
        server.setUpdateListener(updateListener);
    }

    public void send(ChatEvent event) {
        server.send(event);
    }

    /**
     * Called when game finishes, ends the game loop
     *
     * @param endMessage reason for game ending
     */
    public void endGame(String endMessage) {

    }
}
