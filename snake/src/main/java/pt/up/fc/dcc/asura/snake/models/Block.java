package pt.up.fc.dcc.asura.snake.models;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.snake.utils.Sprite;
import pt.up.fc.dcc.asura.snake.utils.SpriteManager;
import pt.up.fc.dcc.asura.snake.utils.Vector;

import static pt.up.fc.dcc.asura.snake.SnakeState.TILE_LENGTH;

/**
 * Block of a barrier in Snake map
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Block extends Actor {

    private Sprite sprite;

    public Block(Vector position) {
        super(position, new Vector(0, 0));
        this.sprite = SpriteManager.getSpriteFor(this);
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void draw(GameMovieBuilder builder) {

        builder.addItem(sprite.getName(),
                TILE_LENGTH * getPosition().getX(),
                TILE_LENGTH * getPosition().getY());
    }
}
