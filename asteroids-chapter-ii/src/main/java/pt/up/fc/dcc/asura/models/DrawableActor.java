package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.AsteroidsState;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.utils.Vector;

import java.util.logging.Logger;

/**
 * An actor that can be drawn in the game world.
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public abstract class DrawableActor extends Actor {
    private static final int DEFAULT_SPRITE_SIZE = 64;

    protected String spriteId;

    protected int spriteSize;

    // length of the animation in frames
    protected int animationLength = 0;
    // direction of the animation
    protected boolean animationForward = true;
    // speed of the animation
    protected double animationSpeed = 1D;
    // frame index of the animation
    protected double animationFrame = 0D;
    // direction of the animation (true - horizontal, false - vertical)
    protected boolean animationHorizontal = false;

    protected DrawableActor(String spriteId, Vector position, Vector velocity) {
        this(spriteId, DEFAULT_SPRITE_SIZE, position, velocity);
    }

    protected DrawableActor(String spriteId, int spriteSize, Vector position, Vector velocity) {
        super(position, velocity);
        this.spriteId = spriteId;
        this.spriteSize = spriteSize;
    }

    /**
     * Draw a sprite on the game movie.
     *
     * @param builder  {@link GameMovieBuilder} game movie builder
     * @param relX     {@code int} x position relative to actor position
     * @param relY     {@code int} y position relative to actor position
     * @param size     {@code double} size to draw
     * @param rotation {@code double} rotation of the sprite
     */
    public void drawSprite(GameMovieBuilder builder, int relX, int relY, int size, double rotation) {

        renderSprite(builder, spriteId,
                (int) (position.getX() + relX),
                (int) (position.getY() + relY),
                rotation, (double) size / spriteSize,
                animationHorizontal ? ((int) animationFrame) * spriteSize : 0,
                animationHorizontal ? 0 : ((int) animationFrame) * spriteSize,
                spriteSize);

        // update animation frame index
        if (animationForward) {
            animationFrame += animationSpeed;

            if (animationFrame >= animationLength)
                animationFrame = 0;
        } else {
            animationFrame -= animationSpeed;
            if (animationFrame < 0)
                animationFrame = animationLength - 1;
        }
    }

    /**
     * Render the sprite into the movie considering an endless canvas.
     *
     * @param builder {@link GameMovieBuilder} game movie builder
     * @param spriteId {@link String} ID of the sprite
     * @param x {@code int} x-coordinate of the {@link Actor}
     * @param y {@code int} y-coordinate of the {@link Actor}
     * @param rotation {@code double} rotation of the draw
     * @param scale {@code double} scale to draw
     * @param offsetX {@code int} offset in pixels of the horizontal start coordinate of the view window
     * @param offsetY {@code int} offset in pixels of the horizontal start coordinate of the view window
     * @param spriteSize {@code int} size of the sprite (it's always a square)
     */
    private void renderSprite(GameMovieBuilder builder, String spriteId, int x, int y, double rotation, double scale,
                              int offsetX, int offsetY, int spriteSize) {

        builder.addItem(spriteId, x, y,
                rotation, scale,
                offsetX, offsetY,
                spriteSize, spriteSize);

        if (x - spriteSize < 0)
            builder.addItem(spriteId,
                    AsteroidsState.GAME_WORLD_WIDTH + x, y,
                    rotation, scale,
                    offsetX, offsetY,
                    spriteSize, spriteSize);
        if (y - spriteSize < 0)
            builder.addItem(spriteId,
                    x, AsteroidsState.GAME_WORLD_HEIGHT + y,
                    rotation, scale,
                    offsetX, offsetY,
                    spriteSize, spriteSize);
        if (x - spriteSize < 0 && y - spriteSize < 0)
            builder.addItem(spriteId,
                    AsteroidsState.GAME_WORLD_WIDTH + x, AsteroidsState.GAME_WORLD_HEIGHT + y,
                    rotation, scale,
                    offsetX, offsetY,
                    spriteSize, spriteSize);
        if (x + spriteSize > AsteroidsState.GAME_WORLD_WIDTH)
            builder.addItem(spriteId,
                    x - AsteroidsState.GAME_WORLD_WIDTH, y,
                    rotation, scale,
                    offsetX, offsetY,
                    spriteSize, spriteSize);
        if (y + spriteSize > AsteroidsState.GAME_WORLD_HEIGHT)
            builder.addItem(spriteId,
                    x, y - AsteroidsState.GAME_WORLD_HEIGHT,
                    rotation, scale,
                    offsetX, offsetY,
                    spriteSize, spriteSize);
        if (x + spriteSize > AsteroidsState.GAME_WORLD_WIDTH && y + spriteSize > AsteroidsState.GAME_WORLD_HEIGHT)
            builder.addItem(spriteId,
                    x - AsteroidsState.GAME_WORLD_WIDTH, y - AsteroidsState.GAME_WORLD_HEIGHT,
                    rotation, scale,
                    offsetX, offsetY,
                    spriteSize, spriteSize);
    }
}
