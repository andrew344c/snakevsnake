package GameComponents;

import java.io.Serializable;

/**
 * Simple struct representing a cell in the grid
 * @author Andrew, Richard
 */
public class Cell implements Serializable {

    private boolean food;
    private boolean snake;
    private boolean headUp;
    private boolean headRight;
    private boolean headDown;
    private boolean headLeft;
    private boolean biteUp;
    private boolean biteRight;
    private boolean biteDown;
    private boolean biteLeft;
    private int x;
    private int y;

    /**
     * Constructor
     * @param x x location
     * @param y y location
     */
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        food = false;
        snake = false;
        headUp = false;
        headRight = false;
        headDown = false;
        headLeft = false;
        biteUp = false;
        biteRight = false;
        biteDown = false;
        biteLeft = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean hasFood() {
        return food;
    }

    public boolean hasSnakeHeadUp() { return headUp;}

    public void setSnakeHeadUp(boolean headUp) { this.headUp = headUp;}

    public boolean hasSnakeHeadRight() { return headRight;}

    public void setSnakeHeadRight(boolean headRight) { this.headRight = headRight;}

    public boolean hasSnakeHeadDown() { return headDown;}

    public void setSnakeHeadDown(boolean headDown) { this.headDown = headDown;}

    public boolean hasSnakeHeadLeft() { return headLeft;}

    public void setSnakeHeadLeft(boolean headLeft) { this.headLeft = headLeft;}

    public void setSnakeBiteUp(boolean biteUp) { this.biteUp = biteUp;}

    public boolean hasSnakeBiteUp(){ return biteUp; }

    public void setSnakeBiteRight(boolean biteRight) { this.biteRight = biteRight;}

    public boolean hasSnakeBiteRight(){ return biteRight; }

    public void setSnakeBiteDown(boolean biteDown) { this.biteDown = biteDown;}

    public boolean hasSnakeBiteDown(){ return biteDown; }

    public void setSnakeBiteLeft(boolean biteLeft) { this.biteLeft = biteLeft;}

    public boolean hasSnakeBiteLeft(){ return biteLeft; }

    public void setAllFalse() { headUp = false; headRight = false; headDown = false; headLeft = false; biteUp = false; biteRight = false; biteDown = false; biteLeft = false;}

    public boolean hasSnake() {
        return snake;
    }

    public void setFood(boolean newFood) {
        food = newFood;
    }

    public void setSnake(boolean newSnake) {
        snake = newSnake;
    }

    public String toString() {
        String coords = "(" + x + ", " + y + ")";
        if (hasFood() && hasSnake()) {
            return "X" + coords; // Not suppose to happen
        }else if (hasSnake()) {
            return "S" + coords;
        }else if (hasFood()) {
            return "F" + coords;
        }else {
            return "0" + coords;
        }

    }
}