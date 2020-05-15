package GameComponents;

import java.util.*;

/**
 * Class representing the grid where snake moves in
 *
 * TODO: Implement a better approach to generating food (Probably not needed, since grid wouldn't be massive)
 *
 * @author Andrew
 */
public class Grid {

    private Cell[][] grid;
    private boolean hasFood;

    public Grid(int rows, int cols) {
        grid = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Cell(j, i);
            }
        }
    }

    public Cell at(int x, int y) {
        return grid[y][x];
    }

    public int getRows() {
        return grid.length;
    }

    public int getCols() {
        return grid[0].length;
    }

    public boolean inGrid(int x, int y) {
        return x >= 0 && x < grid[0].length && y >= 0 && y < grid.length;
    }

    public boolean inSnake(Cell cell) {
        return cell.hasSnake();
    }

    /**
     *
     * Updates the cell at that location
     * Only used in multi-player
     *
     * @param cell to be updated
     */
    public void updateCell(Cell cell) {
        grid[cell.getY()][cell.getX()] = cell;
    }

    public boolean hasFood() {
        return hasFood;
    }

    public void setHasFood(boolean hasFood) {
        this.hasFood = hasFood;
    }

    /**
     * For now just a naive implementation of generating food by just searching through whole grid for
     * all free locations and then randomly picking one of those free locations
     */
    public Cell generateFood() {
        ArrayList<Cell> possibleCells = new ArrayList<Cell>();
        for (Cell[] row: grid) {
            for (Cell cell: row) {
                if (!cell.hasSnake()) {
                    possibleCells.add(cell);
                }
            }
        }
        Cell chosenCell = possibleCells.get(new Random().nextInt(possibleCells.size()));
        chosenCell.setFood(true);
        hasFood = true;
        return chosenCell;
    }

    /**
     * For debugging uses
     * @return String representation of grid
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Cell[] row: grid) {
            for (Cell cell: row) {
                s.append(cell.toString()).append(" ");
            }
            s.append("\n");
        }
        return s.toString();
    }
}