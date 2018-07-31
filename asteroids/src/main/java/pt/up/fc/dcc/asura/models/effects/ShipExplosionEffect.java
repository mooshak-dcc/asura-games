package pt.up.fc.dcc.asura.models.effects;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.utils.Vector;

public class ShipExplosionEffect extends EffectActor {
    private static final String SPRITE_ID = "se";
    private static final String SPRITE = "se.png";
    private static final int SPRITE_SIZE = 64;
    private static final double EXPLOSION_RADIUS = 30D;
    private static final int ANIMATION_LENGTH = 16;

    private int heading;

    public ShipExplosionEffect(Vector position, int heading, int size) {
        this(position, heading, new Vector(0, 0));
    }

    public ShipExplosionEffect(Vector position, int heading, Vector velocity) {
        super(SPRITE_ID, SPRITE_SIZE, position, velocity, ANIMATION_LENGTH);
        this.position = position;
        this.heading = heading;

        this.animationLength = ANIMATION_LENGTH;
        this.animationHorizontal = true;
    }

    @Override
    public void draw(GameMovieBuilder builder) {
        drawSprite(builder, 0, 0, (int) (EXPLOSION_RADIUS * 2), heading);
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
