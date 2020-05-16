package GameComponents;

import Networking.ClientService;
import Networking.SocketInexistentException;

import java.awt.event.KeyEvent;
import java.io.IOException;
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

    private final String LOST_SIGNAL = "[S]LOST";
    private final String CONTINUE_SIGNAL = "[S]CONTINUE";

    /**
     * Single-player constructor
     *
     * @param rows
     * @param cols
     */
    public Game(int rows, int cols) {
        //Setup Game Components (Game.Grid and Game.Snake)
        grid = new Grid(rows, cols);
        player = new Snake(grid.at(cols / 2, rows / 2), grid);
        updatedCells = new ArrayList<Cell>();
        try {
            player.update();
        } catch (SnakeOutOfBoundsException ignored) {}
        player.getHead().setSnake(true);
        updatedCells.add(player.getHead());
        updatedCells.add(grid.generateFood());
        lost = false;
    }

    public Game(String ip, int port) {
        server = new ClientService(ip, port);
        server.setLock(this);
        Object[] snakeAndGridAndUpdate = server.initializeGame();    //blocking
        grid = (Grid)snakeAndGridAndUpdate[1];
        player = new Snake((Cell)snakeAndGridAndUpdate[0], grid);
        updatedCells = (ArrayList<Cell>)snakeAndGridAndUpdate[2];
        serverThread = new Thread(server);
        serverThread.start();
        lost = false;
    }

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
                try {
                    server.send(LOST_SIGNAL);
                }catch (IOException e) {
                    throw new SocketInexistentException(server.IP, server.PORT);
                }
                // Updated Cells is updated in killSnake call
                try {
                    server.send(updatedCells);
                } catch (IOException e) {
                    throw new SocketInexistentException(server.IP, server.PORT);
                }
            }else {
                //sketchy dush
                //sending "projection" of head and then changing back

                boolean previous = player.getHead().hasSnake();
                player.getHead().setSnake(true);
                try {
                    server.send(updatedCells);
                } catch (IOException e) {
                    throw new SocketInexistentException(server.IP, server.PORT);
                }
                player.getHead().setSnake(previous);
            }

            //Wait for and update from server
            acceptUpdate();
        }

        // Food generation: if client eats food, is responsible for generating new food and sends to server
        Cell food = null;
        if (!lost) {
            if (player.isMoving()) {
                if (!player.getHead().hasSnake()) {
                    player.getHead().setSnake(true);    // If no snake in front, approve snake head's location other wise send lost signal
                    if (player.ate) {
                        food = grid.generateFood();
                        player.ate = false;
                        updatedCells.add(food);
                    }
                } else {
                    updatedCells = player.killSnake();
                    lost = true;
                }
            }

            //If hits another snake
            if (server != null && lost) {
                try {
                    server.send(LOST_SIGNAL);
                } catch (IOException e) {
                    throw new SocketInexistentException(server.IP, server.PORT);
                }
                try {
                    server.send(updatedCells);
                } catch (IOException e) {
                    throw new SocketInexistentException(server.IP, server.PORT);
                }
            }
        }

        // Send food update to server and also receive updates from server (these update could be dead snakes and food)
        if (server != null) {
            // Send food
            if (food != null) {
                try {
                    server.send(new ArrayList<Cell>(Collections.singletonList(food)));
                } catch (IOException e) {
                    throw new SocketInexistentException(server.IP, server.PORT);
                }
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

    public synchronized void ready() {
        this.notify();
    }

    public void acceptUpdate() throws InterruptedException{
        // Wait for update from server
        if (server.getUpdate() == null) {
            synchronized (this) {
                this.wait();
            }
        }

        // Add update to server
        for (Cell cell: server.getUpdate()) {
            updatedCells.add(cell);
            grid.updateCell(cell);
        }
    }
}
