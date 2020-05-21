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
 * TODO: Improve GUI (Currently Basic Grid)
 * TODO: Implement multi-player support
 *
 * @author Andrew
 */
public class GamePanel extends JPanel implements ActionListener {

    private CellView[][] gridWindow;
    private static final int DELAY = 2000;
    private Timer timer;
    private Game game;

    public GamePanel(int rows, int cols) {
        //Setup gui grid
        setLayout(new GridLayout(rows, cols));
        gridWindow = new CellView[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gridWindow[i][j] = new CellView(Color.WHITE);
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
                gridWindow[i][j] = new CellView(Color.WHITE);
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
            if (cell.hasSnake()) {
                gridWindow[cell.getY()][cell.getX()].setColor(Color.GREEN);
            }else if (cell.hasFood()) {
                gridWindow[cell.getY()][cell.getX()].setColor(Color.RED);
            }else {
                gridWindow[cell.getY()][cell.getX()].setColor(Color.WHITE);
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
        private static final int CELL_SIZE = 20;
        private static final int BORDER_SIZE = 1;
        public CellView(Color color) {
            setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
            setOpaque(true);
            setBackground(color);
            setBorder(BorderFactory.createLineBorder(Color.BLACK, BORDER_SIZE));
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
