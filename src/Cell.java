import java.awt.Color;

/**
 * Simple struct representing a cell in the grid
 * @author Andrew
 */
public class Cell {

    private boolean food;
    private boolean snake;
    private int x;
    private int y;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        food = false;
        snake = false;
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
        if (hasFood() && hasSnake()) {
            return "X"; // Not suppose to happen
        }else if (hasSnake()) {
            return "S";
        }else if (hasFood()) {
            return "F";
        }else {
            return "0";
        }

    }
}