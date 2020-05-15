package GameComponents;

import Networking.ClientService;
import Networking.SocketInexistentException;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;

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

    private final Object lock = new Object();

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
        server.setLock(lock);
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
            updatedCells = player.update(); //Will return [head ...]
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
                player.getHead().setSnake(true);
                try {
                    server.send(updatedCells);
                } catch (IOException e) {
                    throw new SocketInexistentException(server.IP, server.PORT);
                }
                player.getHead().setSnake(false);
            }

            if (server.getUpdate() == null) {
                synchronized (lock) {
                    lock.wait();
                }
            }

            for (Cell cell: server.getUpdate()) {
                updatedCells.add(cell);
                grid.updateCell(cell);
            }

            //Update Grid with new locations

            // Send ONLY updated location to server instead of sending whole snake/grid (can allow easy upscaling to more than 2 players)
            // Note: Will have to send a "projection" of what the head would be, as the head hasn't been updated yet
            // Receive all necessary updates and updates grid accordingly
            // NOTE: Is only a possible approach, may switch to different approach later
        }


        Cell food = null;
        if (!lost) {
            if (player.isMoving()) {
                if (!player.getHead().hasSnake()) {
                    player.getHead().setSnake(true);
                    if (!grid.hasFood()) {
                        food = grid.generateFood();
                        updatedCells.add(food);
                    }
                } else {
                    lost = true;
                }
            }

            if (server != null) {
                if (lost) {
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
        }

        if (server != null) {
            if (food != null) {
                try {
                    server.send(food);
                } catch (IOException e) {
                    throw new SocketInexistentException(server.IP, server.PORT);
                }
            } else {
                try {
                    server.send("CONTINUE");
                } catch (IOException e) {
                    throw new SocketInexistentException(server.IP, server.PORT);
                }
            }

            if (server.getUpdate() == null) {
                synchronized (lock) {
                    lock.wait();
                }
            }

            for (Cell cell: server.getUpdate()) {
                updatedCells.add(cell);
                grid.updateCell(cell);
            }
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
}
