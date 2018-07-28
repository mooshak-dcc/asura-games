package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;

/**
 * Representation of a star
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Star implements Drawable {
    public static final String SPRITE_ID_FORMAT = "star-%d";
    public static final String[] SPRITES = { "star.png" };
    public static final int MAX_Z = 12;
    public static final double VELOCITY = 0.85;

    private int x;
    private int y;
    private int z;
    private int prevX;
    private int prevY;

    private Star(int x, int y, int z) {
        this.prevX = 0;
        this.prevY = 0;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Star create() {
        return new Star(0, 0, 0);
    }

    @Override
    public void draw(GameMovieBuilder builder) {

    }

    /**
     * Load all sprites to the {@link GameMovieBuilder} builder
     *
     * @param builder {@link GameMovieBuilder} builder
     */
    public static void loadSprites(GameMovieBuilder builder) {
        for (int i = 0; i < SPRITES.length; i++) {
            builder.addSprite(String.format(SPRITE_ID_FORMAT, i), SPRITES[i]);
        }
    }
}
