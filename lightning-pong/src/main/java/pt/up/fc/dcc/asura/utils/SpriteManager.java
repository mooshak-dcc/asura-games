package pt.up.fc.dcc.asura.utils;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.models.*;

import static pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder.SpriteAnchor;

/**
 * Manages sprites of the game
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class SpriteManager {
    private static final String EXTENSION = "png";

    private static final Sprite[] PADDLES = {
            new Sprite("p1", EXTENSION, SpriteAnchor.CENTER, 40, 140, 10, 110),
            new Sprite("p2", EXTENSION, SpriteAnchor.CENTER, 40, 140, 10, 110),
            new Sprite("p3", EXTENSION, SpriteAnchor.CENTER, 40, 140, 10, 110),
            new Sprite("p3", EXTENSION, SpriteAnchor.CENTER, 40, 140, 10, 110)
    };
    private static final Sprite BALL =
            new Sprite("b", EXTENSION, SpriteAnchor.CENTER, 51, 51, 25, 25);
    private static final Sprite[] POWER_UPS = {
            new Sprite("pu1", EXTENSION, SpriteAnchor.CENTER, 1024, 128, 120, 120, 128, 128),
            new Sprite("pu2", EXTENSION, SpriteAnchor.CENTER, 1024, 128, 120, 120, 128, 128),
            new Sprite("pu3", EXTENSION, SpriteAnchor.CENTER, 1024, 128, 120, 120, 128, 128)
    };
    private static final Sprite[] LASERS = {
            new Sprite("l1", EXTENSION, SpriteAnchor.CENTER, 768, 256, 175, 175, 256, 256),
            new Sprite("l2", EXTENSION, SpriteAnchor.CENTER, 768, 256, 175, 175, 256, 256)
    };
    private static final Sprite CHARACTERS =
            new Sprite("chrs", EXTENSION, SpriteAnchor.CENTER, 610, 650, 61, 65, 61, 65);

    public static void load(GameMovieBuilder builder) {

        builder.addSprite(BALL.getName(), BALL.getFilename());

        for (Sprite paddle: PADDLES)
            builder.addSprite(paddle.getName(), paddle.getFilename());

        for (Sprite powerUp: POWER_UPS)
            builder.addSprite(powerUp.getName(), powerUp.getFilename());

        for (Sprite laser: LASERS)
            builder.addSprite(laser.getName(), laser.getFilename());

        builder.addSprite(CHARACTERS.getName(), CHARACTERS.getFilename());
    }

    public static Sprite getSpriteFor(Actor actor) {
        return getSpriteFor(actor, 0);
    }

    public static Sprite getSpriteFor(Actor actor, int order) {

        if (actor instanceof Ball)
            return BALL;
        else if (actor instanceof Paddle)
            return PADDLES[order];
        else if (actor instanceof Laser)
            return LASERS[order];
        else if (actor instanceof PowerUp)
            return POWER_UPS[order];
        else
            return CHARACTERS;
    }
}
