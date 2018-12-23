package pt.up.fc.dcc.asura.snake.models;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.snake.utils.Sprite;
import pt.up.fc.dcc.asura.snake.utils.SpriteManager;
import pt.up.fc.dcc.asura.snake.utils.Vector;

import java.util.LinkedList;

import static pt.up.fc.dcc.asura.snake.SnakeState.TILE_LENGTH;

/**
 * Snake actor
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Snake extends Actor {
    private static final int SPEED = 1;
    private static final Direction DEFAULT_DIRECTION = Direction.RIGHT;
    private static final int START_LENGTH = 3;

    private Sprite sprite;

    private Direction direction = DEFAULT_DIRECTION;
    private Direction lastDirection = DEFAULT_DIRECTION;

    private LinkedList<TailSection> tail = new LinkedList<>();

    private boolean grow = false;

    public Snake() {
        super(new Vector(0, 0), new Vector(SPEED, 0));
        sprite = SpriteManager.getSpriteFor(this);
        sprite = sprite.scaleObject((double) TILE_LENGTH / sprite.getObjectWidth());
    }

    public void buildTail() {

        for (int i = 1; i < START_LENGTH; i++) {

            TailSection section = new TailSection(
                    new Vector(
                            position.getX() + i * (direction == Direction.RIGHT ? -SPEED : (direction == Direction.LEFT ? SPEED : 0)),
                            position.getY() + i * (direction == Direction.DOWN ? -SPEED : (direction == Direction.UP ? SPEED : 0))),
                    sprite, direction, i == START_LENGTH - 1);
            tail.add(section);
        }
    }

    public LinkedList<TailSection> tail() {
        return tail;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean up() {

        if (direction == Direction.DOWN)
            return false;

        lastDirection = direction;
        direction = Direction.UP;
        velocity = new Vector(0, -SPEED);

        return true;
    }

    public boolean down() {

        if (direction == Direction.UP)
            return false;

        lastDirection = direction;
        direction = Direction.DOWN;
        velocity = new Vector(0, SPEED);

        return true;
    }

    public boolean left() {

        if (direction == Direction.RIGHT)
            return false;

        lastDirection = direction;
        direction = Direction.LEFT;
        velocity = new Vector(-SPEED, 0);

        return true;
    }

    public boolean right() {

        if (direction == Direction.LEFT)
            return false;

        lastDirection = direction;
        direction = Direction.RIGHT;
        velocity = new Vector(SPEED, 0);

        return true;
    }

    public void eat() {
        grow = true;
    }

    @Override
    public void onUpdate() {

        // get the position of the last segment
        TailSection lastSection = tail.getLast();
        Vector lastSectionPosition = (Vector) lastSection.getPosition().clone();
        Direction lastSectionDirection = lastSection.getDirection();

        // move segments to the position of the previous segment
        for (int i = tail.size() - 1; i >= 1; i--) {
            tail.get(i).drag(tail.get(i - 1).getDirection());
            ///tail.get(i).setPosition(tail.get(i - 1).getPosition());
        }
        tail.get(0).drag(direction);

        //tail.get(0).setPosition(position);

        // grow a segment if needed
        if (grow) {
            tail.getLast().setLast(false);
            tail.addLast(new TailSection(lastSectionPosition, sprite, lastSectionDirection, true));
            grow = false;
        }

        // move the head
        position = position.add(velocity);

        lastDirection = direction;
    }

    @Override
    public void draw(GameMovieBuilder builder) {

        int animFrameIdx;
        if (velocity.getY() < 0)
            animFrameIdx = 0;
        else if (velocity.getX() > 0)
            animFrameIdx = 1;
        else if (velocity.getY() > 0)
            animFrameIdx = 2;
        else
            animFrameIdx = 3;

        Sprite.AnimationFrame animFrame = sprite.getAnimationFrame(animFrameIdx);

        builder.addItem(sprite.getName(),
                position.getX() * TILE_LENGTH, position.getY() * TILE_LENGTH,
                0, (double) TILE_LENGTH / sprite.getAnimationWidth(),
                animFrame.getX(), animFrame.getY(), animFrame.getWidth(), animFrame.getY());

        for (TailSection section: tail)
            section.draw(builder);
    }

    /**
     * Check if {@link Vector} pos overlaps the snake
     *
     * @param pos {@link Vector} position to check
     * @return {@code boolean} true if pos overlaps the snake, false otherwise
     */
    public boolean overlaps(Vector pos) {

        if (position.getX() == pos.getX() && position.getY() == pos.getY())
            return true;

        for (TailSection section: tail) {

            if (section.getPosition().getX() == pos.getX()
                    && section.getPosition().getY() == pos.getY())
                return true;
        }

        return false;
    }

    public boolean eatsTail() {
        for (TailSection section: tail) {

            if (section.getPosition().getX() == position.getX()
                    && section.getPosition().getY() == position.getY())
                return true;
        }

        return false;
    }
}
