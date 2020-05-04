import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Main handler for game and Gui representation of grid
 * Listens for keystrokes here for snake control
 *
 * TODO: Improve GUI (Currently Basic Grid)
 * TODO: Maybe separate game logic from GUI?
 * TODO: Implement multi-player support
 *
 * @author Andrew
 */
public class GamePanel extends JPanel implements ActionListener {

    private Grid grid;
    private CellView[][] gridWindow;
    private Snake player;
    private boolean multiplayer;
    private final int DELAY = 125;
    private Timer timer;

    public GamePanel(int rows, int cols, boolean multiplayer) {     //multiplayer set to boolean temp change later
        this.multiplayer = multiplayer;

        //Setup gui grid
        setLayout(new GridLayout(rows, cols));
        gridWindow = new CellView[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gridWindow[i][j] = new CellView(Color.WHITE);
                add(gridWindow[i][j]);
            }
        }

        //Setup Game Components (Grid and Snake)
        grid = new Grid(rows, cols);

        if (!multiplayer) {
            player = new Snake(grid.at(cols / 2, rows / 2), grid);
            player.update();
            player.getHead().setSnake(true);
            updateGuiGrid(new ArrayList<Cell>(Arrays.asList(player.getHead(), grid.generateFood())));
        }

        addKeyListener(new ControllerAdapter());
        setFocusable(true);

        timer = new Timer(DELAY, this);
        timer.start();
    }

    /**
     * Update inner grid (used in multi-player)
     * @param changedLocations
     */
    public void updateGrid(Cell[] changedLocations) {
        for (Cell cell: changedLocations) {
            grid.updateCell(cell);
        }
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
        ArrayList<Cell> changedLocations = new ArrayList<Cell>();

        // Update snake body (Will also update tail in grid directly in update call, but head will not be updated in grid)
        try {
            changedLocations = player.update(); //Will return [head ...]
        }catch (IndexOutOfBoundsException error) {
            System.out.println("lost dush");
            timer.stop();
        }

        if (multiplayer) {
            // Send ONLY updated location to server instead of sending whole snake/grid (can allow easy upscaling to more than 2 players)
            // Note: Will have to send a "projection" of what the head would be, as the head hasn't been updated yet
            // Receive all necessary updates and updates grid accordingly
            // Send msg back to server indicating operations are done
            // Halt entire program till msg received from server that other player is ready
            // NOTE: Is only a possible approach, may switch to different approach later

            // Also food generation should occur server-side
        }

        if (player.isMoving()) {
            // i hate this but idk how to change it
            // basically head doesn't get changed in update() and don't detect whether or not snake bumps into another snake
            // since we need to get updates from the other players first
            if (!player.getHead().hasSnake()) {
                player.getHead().setSnake(true);
                if (!grid.hasFood()) {
                    changedLocations.add(grid.generateFood());     //Food generation will happen server-side
                }
            }else {
                timer.stop();
            }
        }

        updateGuiGrid(changedLocations);
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
            player.keyPressed(e);
        }
    }

}


