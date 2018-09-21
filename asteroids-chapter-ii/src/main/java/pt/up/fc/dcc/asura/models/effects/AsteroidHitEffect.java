package pt.up.fc.dcc.asura.models.effects;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.utils.Vector;

public class AsteroidHitEffect extends EffectActor {
    private static final String SPRITE_ID = "ah";
    private static final String SPRITE = "ah.png";
    private static final int SPRITE_SIZE = 64;
    private static final int ANIMATION_LENGTH = 4;

    private int heading;
    private int size;

    public AsteroidHitEffect(long startTime, Vector position, int heading, int size) {
        this(startTime, position, heading, new Vector(0, 0), size);
    }

    public AsteroidHitEffect(long startTime, Vector position, int heading, Vector velocity, int size) {
        super(startTime, SPRITE_ID, SPRITE_SIZE, position,
                velocity, ANIMATION_LENGTH * 2);
        this.position = position;
        this.heading = heading;
        this.velocity = velocity;
        this.size = size;

        this.animationLength = ANIMATION_LENGTH;
        this.animationHorizontal = true;
        this.animationSpeed = 0.25;
    }

    @Override
    public void draw(long time, GameMovieBuilder builder) {
        drawSprite(builder, 0, 0, 16 * size, heading);
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