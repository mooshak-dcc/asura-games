package pt.up.fc.dcc.asura.snake.utils;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.snake.models.Actor;
import pt.up.fc.dcc.asura.snake.models.Block;
import pt.up.fc.dcc.asura.snake.models.Fruit;
import pt.up.fc.dcc.asura.snake.models.Snake;

import static pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder.SpriteAnchor;

/**
 * Manages sprites of the game
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class SpriteManager {
    private static final String EXTENSION = "png";

    private static final Sprite SNAKE =
            new Sprite("s", EXTENSION, SpriteAnchor.TOP_LEFT, 896, 64, 64, 64, 64, 64);
    private static final Sprite BLOCK =
            new Sprite("b", EXTENSION, SpriteAnchor.TOP_LEFT, 40, 40);
    private static final Sprite FRUIT =
            new Sprite("f", EXTENSION, SpriteAnchor.TOP_LEFT, 1024, 64, 64, 64, 64, 64);

    public static void load(GameMovieBuilder builder) {
        builder.addSprite(BLOCK.getName(), BLOCK.getFilename());
        builder.addSprite(SNAKE.getName(), SNAKE.getFilename());
        builder.addSprite(FRUIT.getName(), FRUIT.getFilename());
    }

    public static Sprite getSpriteFor(Actor actor) {
        return getSpriteFor(actor, 0);
    }

    public static Sprite getSpriteFor(Actor actor, int order) {

        if (actor instanceof Snake)
            return SNAKE;
        else if (actor instanceof Fruit)
            return FRUIT;
        else if (actor instanceof Block)
            return BLOCK;
        return null;
    }
}
