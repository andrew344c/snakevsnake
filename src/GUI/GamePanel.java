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
import java.util.Arrays;
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

    public GamePanel(int rows, int cols) {
        //Setup gui grid
        //setPreferredSize(new Dimension(getWidth(), getHeight()));
        setLayout(new GridLayout(rows, cols));
        gridWindow = new CellView[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gridWindow[i][j] = new CellView(BACKGROUND_COLOR, false);
                add(gridWindow[i][j]);
            }
        }

        //Setup Game and Update GUI after
        game = new Game(rows, cols, this);
        updateGuiGrid(game.getUpdates());

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                game.keyPressed(e);
            }
        });
        setFocusable(true);

        timer = new Timer(DELAY, this);
        timer.start();
    }

    public GamePanel(String ip, int port) {
        game = new Game(ip, port, this);
        game.setUpdateListener(new UpdateListener() {
            @Override
            public void updateOccurred(Cell update) {
                updateGuiGrid(new ArrayList<Cell>(Collections.singletonList(update)));
            }

            @Override
            public void specialEventOccurred(SpecialEvent event) {
                if (event.getType() == SpecialEvent.START) {
                    start();
                }
            }

        });
        int rows = game.getRows();
        int cols = game.getCols();
        setLayout(new GridLayout(rows, cols));
        gridWindow = new CellView[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gridWindow[i][j] = new CellView(BACKGROUND_COLOR, false);
                add(gridWindow[i][j]);
            }
        }
        updateGuiGrid(game.getUpdates());
        game.startListening();
    }

    public void start() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                game.keyPressed(e);
            }
        });
        setFocusable(true);

        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void updateGuiGrid(ArrayList<Cell> changedLocations) {
        System.out.println(changedLocations);
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
                System.out.println(gridWindow[cell.getY()][cell.getX()]);
                gridWindow[cell.getY()][cell.getX()].setIcon(body);
            } else if (cell.hasFood()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(apple);
            } else {
                gridWindow[cell.getY()][cell.getX()].setIcon(null);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            game.update();
        } catch (InterruptedException ex) {
            timer.stop();
        } catch (SocketInexistentException ex) {
            timer.stop();
            System.out.println(ex.getMessage());
        }
        updateGuiGrid(game.getUpdates());
    }

    private static class CellView extends JLabel {
        private static final int BORDER_SIZE = 1;

        public CellView(Color color, boolean gridLines) {
            setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
            setOpaque(true);
            setBackground(color);
            if (gridLines) {
                setBorder(BorderFactory.createLineBorder(Color.BLACK, BORDER_SIZE));
            }
        }

        public void setColor(Color color) {
            setBackground(color);
        }
    }

    public void setScoreUpdateListener(ScoreUpdateListener listener) {
        game.setScoreUpdateListener(listener);
    }

    public void setChatListener(ChatListener chatListener) {
        game.setChatListener(chatListener);
    }

    public void send(ChatEvent event) {
        game.send(event);
    }

    /**
     * Called when game finishes, ends the game loop
     *
     * @param endMessage reason for game ending
     */
    public void endGame(String endMessage) {

    }
}
