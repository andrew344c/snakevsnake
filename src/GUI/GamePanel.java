package GUI;

import GameComponents.Cell;
import GameComponents.Game;
import Networking.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Gui representation of game grid
 * Listens for keystrokes here for snake control
 *
 *
 * @author Andrew, Richard
 */
public class GamePanel extends JPanel implements ActionListener {

    private CellView[][] gridWindow;
    private static final int DELAY = 125;
    private Timer timer;
    private Game game;
    private KeyAdapter keyListener;

    private static final int CELL_SIZE = 25;
    private static final Color BACKGROUND_COLOR = Color.WHITE;

    private final ImageIcon body = new ImageIcon("resources/snakeBody.png");
    private final ImageIcon headDown = new ImageIcon("resources/snakeDown.png");
    private final ImageIcon headRight = new ImageIcon("resources/snakeRight.png");
    private final ImageIcon headUp = new ImageIcon("resources/snakeUp.png");
    private final ImageIcon headLeft = new ImageIcon("resources/snakeLeft.png");
    private final ImageIcon apple = new ImageIcon("resources/apple.png");
    private final ImageIcon biteUp = new ImageIcon("resources/snakeBiteUp.png");
    private final ImageIcon biteRight = new ImageIcon("resources/snakeBiteRight.png");
    private final ImageIcon biteDown = new ImageIcon("resources/snakeBiteDown.png");
    private final ImageIcon biteLeft = new ImageIcon("resources/snakeBiteLeft.png");

    /**
     * Single Player Constructor
     * @param rows rows on grid
     * @param cols cols on grid
     */
    public GamePanel(int rows, int cols) {
        //Setup gui grid
        setUpGuiGrid(rows, cols);

        //Setup Game and Update GUI after
        game = new Game(rows, cols);
        updateGuiGrid(game.getUpdates());

        start(); // Start game
    }

    /**
     * Multi Player Constructor
     * @param ip ip of server
     * @param port port of server
     */
    public GamePanel(String ip, int port, String name) {
        game = new Game(ip, port, name);    //Setup game and server connection
        game.setUpdateListener(new ServerListener() {

            /**
             * Only used during waiting time before game has started, these cells will be new players that have joined
             * During actual game, the program will block in the Game class while waiting for updates to ensure that the current game is the most updated
             *
             * @param update The location of a new player that joined
             */
            @Override
            public void updateOccurred(Cell update) {
                updateGuiGrid(new ArrayList<Cell>(Collections.singletonList(update)));
            }

            @Override
            public void updateOccurred(ArrayList<Cell> update) {
                updateGuiGrid(update);
            }

            /**
             * Used for when special event is received from server
             * @param event This event may indicate that game has started or ended (may end in win, loss, tie)
             */
            @Override
            public void gameStateEventOccurred(GameStateEvent event) {
                if (event.getType() == GameStateEvent.START) {
                    start();
                }else if (event.getType() == GameStateEvent.LOST) {
                    stop();
                }else if (event.getType() == GameStateEvent.WIN) {
                    stop();
                    String winners = "";
                    for (String player: event.getPlayers()) {
                        winners += player + " ";
                    }
                    JLabel winLabel = new JLabel(winners + "won!", SwingConstants.CENTER);
                    add(winLabel);
                }else if (event.getType() == GameStateEvent.TIE){
                    stop();
                    String tied = "";
                    for (String player: event.getPlayers()) {
                        tied += player + " ";
                    }
                    JLabel tieLabel = new JLabel(tied + "tied!", SwingConstants.CENTER);
                    add(tieLabel);
                }
            }
        }); // Used for direct communication between server updates and gui

        // Get the correct dimensions for the grid
        int rows = game.getRows();
        int cols = game.getCols();
        setUpGuiGrid(rows, cols);
        updateGuiGrid(game.getUpdates());

        game.startListening(); // Let game to continue listening for server updates
    }

    /**
     * Sets up the GUI Grid in a grid layout
     * @param rows rows of the grid
     * @param cols cols of the grid
     */
    public void setUpGuiGrid(int rows, int cols) {
        setLayout(new GridLayout(rows, cols));
        gridWindow = new CellView[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gridWindow[i][j] = new CellView(BACKGROUND_COLOR, false);
                add(gridWindow[i][j]);
            }
        }
    }

    /**
     * Starts game loop and starts listening for keystrokes
     */
    public void start() {
        keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                game.keyPressed(e);
            }
        };
        addKeyListener(keyListener);
        setFocusable(true);

        timer = new Timer(DELAY, this);
        timer.start();
    }

    /**
     * Stop listening for keystrokes and game loop
     * Used when Win, Lost, or Tie Special Event occurs
     */
    public void stop() {
        if (keyListener != null) {
            removeKeyListener(keyListener);
            keyListener = null;
        }

        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    /**
     * Updates the GUI Grid with the provided locations
     * @param changedLocations An array-list of cells that represent cells in the grid to be updated
     */
    public void updateGuiGrid(ArrayList<Cell> changedLocations) {
        for (Cell cell : changedLocations) {
            if (cell.hasSnakeHeadRight()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(headRight);
            } else if (cell.hasSnakeHeadLeft()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(headLeft);
            } else if (cell.hasSnakeHeadUp()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(headUp);
            } else if (cell.hasSnakeHeadDown()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(headDown);
            } else if (cell.hasSnakeBiteUp()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(biteUp);
            } else if (cell.hasSnakeBiteRight()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(biteRight);
            } else if (cell.hasSnakeBiteDown()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(biteDown);
            } else if (cell.hasSnakeBiteLeft()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(biteLeft);
            } else if (cell.hasSnake()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(body);
            } else if (cell.hasFood()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(apple);
            } else {
                gridWindow[cell.getY()][cell.getX()].setIcon(null);
            }
        }
    }

    /**
     * Every "tick" of the game, occurs every `DELAY` time
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            game.update();  // Blocking
        } catch (InterruptedException ex) {
            timer.stop();   // Only used in single player
        }
        updateGuiGrid(game.getUpdates());
    }

    /**
     * JLabel that holds an icon representing a cell in the GUI Grid
     */
    private static class CellView extends JLabel {
        private static final int BORDER_SIZE = 1;

        /**
         * Constructor
         * @param color color of cell
         * @param gridLines include border or not
         */
        public CellView(Color color, boolean gridLines) {
            setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
            setOpaque(true);
            setBackground(color);
            if (gridLines) {
                setBorder(BorderFactory.createLineBorder(Color.BLACK, BORDER_SIZE));
            }
        }

        /**
         * Sets color of cell
         * @param color color to be set
         */
        public void setColor(Color color) {
            setBackground(color);
        }
    }

    /**
     * Sets score update listener
     * Used for direct communication from GUI to the game
     * @param listener Listens for updates in the score
     */
    public void setScoreUpdateListener(ScoreUpdateListener listener) {
        game.setScoreUpdateListener(listener);
    }

    /**
     * Sets chat listener
     * Used for direct communication from GUI to ClientService
     * @param chatListener Listens for chat events
     */
    public void setChatListener(ChatListener chatListener) {
        game.setChatListener(chatListener);
    }

    public void setServerConnectionListener(ServerConnectionListener serverConnectionListener) {
        game.setServerConnectionListener(serverConnectionListener);
    }

    /**
     * Sends a chat message to server
     * Called from GameWindow, which gets chat message to be sent from GameChatPanel
     * @param event ChatEvent to be sent
     */
    public void sendChat(ChatEvent event) {
        game.sendChat(event);
    }

    /**
     * Disconnect from server
     */
    public void disconnect() {
        game.disconnect();
    }
}
