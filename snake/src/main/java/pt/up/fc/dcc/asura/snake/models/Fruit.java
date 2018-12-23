package pt.up.fc.dcc.asura.snake.models;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.snake.utils.Sprite;
import pt.up.fc.dcc.asura.snake.utils.SpriteManager;
import pt.up.fc.dcc.asura.snake.utils.Vector;

import static pt.up.fc.dcc.asura.snake.SnakeState.TILE_LENGTH;

/**
 * Fruit to be catch by the snake
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Fruit extends Actor {

    private Sprite sprite;
    private int animFrameIdx;

    private long time;

    public Fruit(int animFrameIdx, long time) {
        super(new Vector(0, 0), new Vector(0, 0));
        sprite = SpriteManager.getSpriteFor(this);
        sprite = sprite.scaleObject((double) TILE_LENGTH / sprite.getAnimationWidth());
        this.animFrameIdx = animFrameIdx;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void draw(GameMovieBuilder builder) {

        Sprite.AnimationFrame animFrame = sprite.getAnimationFrame(animFrameIdx);

        builder.addItem(sprite.getName(),
                position.getX() * TILE_LENGTH, position.getY() * TILE_LENGTH,
                0, (double) TILE_LENGTH / sprite.getAnimationWidth(),
                animFrame.getX(), animFrame.getY(), animFrame.getWidth(), animFrame.getHeight());
    }
}
