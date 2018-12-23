package pt.up.fc.dcc.asura.snake.messaging;

import pt.up.fc.dcc.asura.snake.utils.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Update of the board
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class BoardUpdate {

    private int width;
    private int height;

    private List<Vector> blocks = new ArrayList<>();

    private boolean newLevel;

    public BoardUpdate(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void addBlock(Vector position) {
        blocks.add(position);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Vector> getBlocks() {
        return blocks;
    }

    public boolean isNewLevel() {
        return newLevel;
    }

    public void setNewLevel(boolean newLevel) {
        this.newLevel = newLevel;
    }
}
