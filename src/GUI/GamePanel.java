package GUI;

import GameComponents.Cell;
import GameComponents.Game;
import Networking.SocketInexistentException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Gui representation of game grid
 * Listens for keystrokes here for snake control
 *
 * @author Andrew
 */
public class GamePanel extends JPanel implements ActionListener {

    private CellView[][] gridWindow;
    private static final int DELAY = 125;
    private Timer timer;
    private Game game;

    private final ImageIcon body = new ImageIcon("resources/snakeBody.png");
    private final ImageIcon headDown = new ImageIcon("resources/snakeDown.png");
    private final ImageIcon headRight = new ImageIcon("resources/snakeRight.png");
    private final ImageIcon headUp = new ImageIcon("resources/snakeUp.png");
    private final ImageIcon headLeft = new ImageIcon("resources/snakeLeft.png");


    public GamePanel(int rows, int cols) {
        //Setup gui grid
        setLayout(new GridLayout(rows, cols));
        gridWindow = new CellView[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gridWindow[i][j] = new CellView(Color.BLACK, false);
                add(gridWindow[i][j]);
            }
        }

        //Setup Game and Update GUI after
        game = new Game(rows, cols, this);
        updateGuiGrid(game.getUpdates());

        addKeyListener(new ControllerAdapter());
        setFocusable(true);

        timer = new Timer(DELAY, this);
        timer.start();
    }

    public GamePanel(String ip, int port) {
        game = new Game(ip, port, this);
        int rows = game.getRows();
        int cols = game.getCols();
        setLayout(new GridLayout(rows, cols));
        gridWindow = new CellView[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gridWindow[i][j] = new CellView(Color.BLACK, false);
                add(gridWindow[i][j]);
            }
        }
        updateGuiGrid(game.getUpdates());

        addKeyListener(new ControllerAdapter());
        setFocusable(true);

        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void updateGuiGrid(ArrayList<Cell> changedLocations) {
        for (Cell cell: changedLocations) {
            if (cell.hasSnakeHeadRight()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(headRight);
                gridWindow[cell.getY()][cell.getX()].setIcon(new ImageIcon(headRight));
                gridWindow[cell.getY()][cell.getX()].setColor(Color.BLACK);
            } else if (cell.hasSnakeHeadLeft()){
                gridWindow[cell.getY()][cell.getX()].setIcon(new ImageIcon(headLeft));
                gridWindow[cell.getY()][cell.getX()].setColor(Color.BLACK);
            } else if (cell.hasSnakeHeadUp()){
                gridWindow[cell.getY()][cell.getX()].setIcon(new ImageIcon(headUp));
                gridWindow[cell.getY()][cell.getX()].setColor(Color.BLACK);
            } else if (cell.hasSnakeHeadDown()){
                gridWindow[cell.getY()][cell.getX()].setIcon(new ImageIcon(headDown));
                gridWindow[cell.getY()][cell.getX()].setColor(Color.BLACK);
            } else if (cell.hasSnakeBiteUp()){
                gridWindow[cell.getY()][cell.getX()].setIcon(new ImageIcon(biteUp));
                gridWindow[cell.getY()][cell.getX()].setColor(Color.BLACK);
            } else if (cell.hasSnakeBiteRight()){
                gridWindow[cell.getY()][cell.getX()].setIcon(new ImageIcon(biteRight));
                gridWindow[cell.getY()][cell.getX()].setColor(Color.BLACK);
            } else if (cell.hasSnakeBiteDown()){
                gridWindow[cell.getY()][cell.getX()].setIcon(new ImageIcon(biteDown));
                gridWindow[cell.getY()][cell.getX()].setColor(Color.BLACK);
            } else if (cell.hasSnakeBiteLeft()){
                gridWindow[cell.getY()][cell.getX()].setIcon(new ImageIcon(biteLeft));
                gridWindow[cell.getY()][cell.getX()].setColor(Color.BLACK);
            }else if (cell.hasSnake()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(body);
                gridWindow[cell.getY()][cell.getX()].setColor(Color.BLACK);
            }else if (cell.hasFood()) {
                gridWindow[cell.getY()][cell.getX()].setIcon(new ImageIcon(apple));
                gridWindow[cell.getY()][cell.getX()].setColor(Color.BLACK);
            }else {
                gridWindow[cell.getY()][cell.getX()].setIcon(null);
                gridWindow[cell.getY()][cell.getX()].setColor(Color.BLACK);
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
        private static final int CELL_SIZE = 25;
        private static final int BORDER_SIZE = 1;
        public CellView(Color color, boolean gridLines) {
            setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
            setOpaque(true);
            setBackground(color);
            if (gridLines){
                setBorder(BorderFactory.createLineBorder(Color.BLACK, BORDER_SIZE));
            }
        }

        public void setColor(Color color) {
            setBackground(color);
        }
    }

    private class ControllerAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            game.keyPressed(e);
        }
    }


    /**
     * Called when game finishes, ends the game loop
     *
     * @param endMessage reason for game ending
     */
    public void endGame(String endMessage) {

    }

}
