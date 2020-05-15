package GameComponents;

import java.util.*;
import java.awt.event.*;


/**
 * Class representing snake
 * @author Andrew
 */
public class Snake {

    private Grid grid;
    private LinkedList<Cell> body; //Game.Cell is deliberately not used
    private int dx;
    private int dy;
    private boolean choseDirection;

    /**
     *
     * Game.Snake Constructor
     * Initializes body with snake of len 1, dx, dy
     *
     * @param head of snake
     */
    public Snake(Cell head, Grid grid) {
        this.grid = grid;
        body = new LinkedList<Cell>();
        body.addFirst(head);
        dx = dy = 0;
        choseDirection = false;
    }

    public Cell getHead() {
        return body.getFirst();
    }

    /**
     * Moves and updates the snake's location accordingly
     * 
     * Changes snake body and returns all cells changed
     *
     * Note: Detecting for hitting into a snake directly here is impossible for multi-player since need to first get
     * other snake updates, but checking for out-of-bounds is done directly as it is not dependent on other snakes
     *
     * Thus the tail is directly changed on grid here, but the head is not and requires further handling
     * 
     * @return An arraylist of all cells that have been changed/updated
     *
     * Note: The 0th item (if there is one) will always be the new head
     * The arraylist will only be of size 0, 1, or 2 (Start: No movement, Eaten: Tail stays same, Normal: Tail and head change)
     */
    public ArrayList<Cell> update() throws SnakeOutOfBoundsException {
        choseDirection = false;
        ArrayList<Cell> changedLocations = new ArrayList<Cell>();
        if (!isMoving()) {
            return changedLocations;
        }
        Cell originalHead = body.getFirst();
        int newX = originalHead.getX() + dx;
        int newY = originalHead.getY() + dy;
        if (!grid.inGrid(newX, newY)) {
            throw new SnakeOutOfBoundsException();
        }

        // i hate this might change later
        Cell newHead = grid.at(originalHead.getX() + dx, originalHead.getY() + dy);
        body.addFirst(newHead);
        changedLocations.add(newHead);

        if (newHead.hasFood()) {
            newHead.setFood(false);
            grid.setHasFood(false);
        }else {
            Cell oldTail = body.removeLast();
            oldTail.setSnake(false);
            changedLocations.add(oldTail);
        }

        return changedLocations;
    }

    public boolean isMoving() {
        return dx != 0 || dy != 0;
    }

    /**
     * Method to handle a key being pressed, changes dx and dy
     * @param e KeyEvent of Key Pressed
     */
    public void keyPressed(KeyEvent e) {
        if (choseDirection) {
            return;
        }
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_UP) {
            if (body.size() == 1 || dy == 0) {  //Will only change that direction if moving in opposite axis (otherwise will collide in self or will be same direction)
                dx = 0;
                dy = -1;
            }
        }else if (keyCode == KeyEvent.VK_DOWN) {
            if (body.size() == 1 || dy == 0) {
                dx = 0;
                dy = 1;
            }
        }else if (keyCode == KeyEvent.VK_LEFT) {
            if (body.size() == 1 || dx == 0) {
                dx = -1;
                dy = 0;
            }
        }else if (keyCode == KeyEvent.VK_RIGHT) {
            if (body.size() == 1 || dx == 0) {
                dx = 1;
                dy = 0;
            }
        }
        choseDirection = true;
    }

    /**
     * Returns the whole body of the snake, for when the snake dies, directly updates local grid
     * @return Cells that was of the snake and need to be updated to now be empty
     */
    public ArrayList<Cell> killSnake() {
        ArrayList<Cell> update = new ArrayList<Cell>();
        for (Cell cell: body) {
            cell.setSnake(false);
            update.add(cell);
        }
        return update;
    }


}