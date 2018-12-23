package pt.up.fc.dcc.asura.snake.levels;

import pt.up.fc.dcc.asura.snake.models.*;
import pt.up.fc.dcc.asura.snake.utils.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Level of the snake game
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Level {
    private static final int[] START_POSITION = new int[] { 6, 2 };

    private int hTiles;
    private int vTiles;

    private List<Block> blocks = new ArrayList<>();

    private Snake snake;

    public Level(int hTiles, int vTiles) {
        this.hTiles = hTiles;
        this.vTiles = vTiles;
    }

    public int getWidth() {
        return hTiles;
    }

    public int getHeight() {
        return vTiles;
    }

    public void addBlock(int x, int y) {
        blocks.add(new Block(new Vector(x, y)));
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void placeSnake(Snake snake) {

        // always the same position for now
        snake.setPosition(new Vector(START_POSITION[1], START_POSITION[0]));

        this.snake = snake;
    }

    public void placeFruit(Fruit fruit) {

        int ax = 0, ay = 0;

        // loop until we have a valid position
        boolean valid = false;
        while (!valid) {

            // get a random position
            ax = (int) (Math.random() * hTiles);
            ay = (int) (Math.random() * vTiles);

            // make sure the snake doesn't overlap the new fruit
            if (snake != null)
                valid = !snake.overlaps(new Vector(ax, ay));

            // tile must be empty
            if (valid) {

                for (Block block: blocks) {

                    if (block.getPosition().getX() == ax && block.getPosition().getY() == ay) {
                        valid = false;
                        break;
                    }
                }
            }
        }

        fruit.setPosition(new Vector(ax, ay));
    }

    public boolean isSnakeOutOfBounds() {

        return snake.getPosition().getX() < 0 || snake.getPosition().getX() >= hTiles ||
                snake.getPosition().getY() < 0 || snake.getPosition().getY() >= vTiles;
    }

    public boolean isSnakeCrashedInBlocks() {

        for (Block block: blocks) {

            if (collides(snake, block))
                return true;
        }

        return false;
    }

    public boolean collides(Actor actor, Actor other) {

        return actor.getPosition().getX() == other.getPosition().getX() &&
                actor.getPosition().getY() == other.getPosition().getY();
    }

    public int[][] asIntArray() {

        int board[][] = new int[vTiles][hTiles];

        for (Block block: blocks)
            board[block.getPosition().getY()][block.getPosition().getX()] = 1;

        board[snake.getPosition().getY()][snake.getPosition().getX()] = 2;
        for (TailSection section: snake.tail())
            board[section.getPosition().getY()][section.getPosition().getX()] = 2;

        return board;
    }


}
