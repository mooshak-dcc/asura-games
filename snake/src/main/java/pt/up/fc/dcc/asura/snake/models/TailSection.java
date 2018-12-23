package pt.up.fc.dcc.asura.snake.models;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.snake.utils.Sprite;
import pt.up.fc.dcc.asura.snake.utils.Vector;

import static pt.up.fc.dcc.asura.snake.SnakeState.TILE_LENGTH;

/**
 * Section of the snake tail
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class TailSection extends Actor {

    private Sprite sprite;

    private Direction direction;
    private boolean last;

    private Direction lastDirection = null;

    public TailSection(Vector position, Sprite sprite, Direction direction, boolean last) {
        super(position, new Vector(0, 0));
        this.sprite = sprite;
        this.direction = direction;
        this.last = last;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isLast() {
        return last;
    }

    @Override
    public void onUpdate() {
        // this actor is updated through drag method by Snake


    }

    public void drag(Direction newDirection) {

        lastDirection = direction;

        switch (lastDirection) {

            case UP:
                position = position.add(new Vector(0, -1));
                break;
            case RIGHT:
                position = position.add(new Vector(1, 0));
                break;
            case DOWN:
                position = position.add(new Vector(0, 1));
                break;
            case LEFT:
                position = position.add(new Vector(-1, 0));
                break;
        }

        direction = newDirection;
    }

    @Override
    public void draw(GameMovieBuilder builder) {

        Sprite.AnimationFrame animFrame = sprite.getAnimationFrame(getAnimationFrameIndex());

        builder.addItem(sprite.getName(),
                position.getX() * TILE_LENGTH, position.getY() * TILE_LENGTH,
                0, (double) TILE_LENGTH / sprite.getAnimationWidth(),
                animFrame.getX(), animFrame.getY(), animFrame.getWidth(), animFrame.getHeight());
    }

    private int getAnimationFrameIndex() {

        int animFrameIdx = 4;

        if (last)
            return animFrameIdx + direction.ordinal();
        else
            animFrameIdx += 4;

        if (direction != lastDirection)
            animFrameIdx += 2;
        else
            return animFrameIdx +
                    (direction == Direction.UP || direction == Direction.DOWN ? 0 : 1);

        if (lastDirection == Direction.RIGHT && direction == Direction.UP
                || lastDirection == Direction.DOWN && direction == Direction.LEFT)
            return animFrameIdx;
        else if (lastDirection == Direction.UP && direction == Direction.RIGHT
                || lastDirection == Direction.LEFT && direction == Direction.DOWN)
            return animFrameIdx + 1;
        else if (lastDirection == Direction.LEFT && direction == Direction.UP
                || lastDirection == Direction.DOWN && direction == Direction.RIGHT)
            return animFrameIdx + 2;
        else
            return animFrameIdx + 3;
    }
}
