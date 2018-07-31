package pt.up.fc.dcc.asura.models.effects;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.utils.Vector;

public class AsteroidExplosionEffect extends EffectActor {
    private static final String SPRITE_ID = "ae";
    private static final String SPRITE = "ae.png";
    private static final int SPRITE_SIZE = 64;
    private static final double EXPLOSION_RADIUS = 16D;
    private static final int ANIMATION_LENGTH = 30;

    private Vector position;
    private int heading;
    private int size;

    public AsteroidExplosionEffect(Vector position, int heading, int size) {
        this(position, heading, new Vector(0, 0), size);
    }

    public AsteroidExplosionEffect(Vector position, int heading, Vector velocity, int size) {
        super(SPRITE_ID, SPRITE_SIZE, position,
                velocity, ANIMATION_LENGTH);
        this.position = position;
        this.heading = heading;
        this.size = size;

        this.animationLength = ANIMATION_LENGTH;
        this.animationHorizontal = true;
    }

    @Override
    public void draw(GameMovieBuilder builder) {
        drawSprite(builder, 0, 0, (int) (EXPLOSION_RADIUS * 2 * size), heading);
    }

    /**
     * Load all sprites to the {@link GameMovieBuilder} builder
     *
     * @param builder {@link GameMovieBuilder} builder
     */
    public static void loadSprites(GameMovieBuilder builder) {
        builder.addSprite(SPRITE_ID, SPRITE);
    }
}
