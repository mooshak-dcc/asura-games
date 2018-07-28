package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.utils.Vector;

public class ShipHitEffect extends EffectActor {
    private static final String SPRITE_ID = "sh";
    private static final String SPRITE = "sh.png";
    private static final int SPRITE_SIZE = 64;
    private static final int ANIMATION_LENGTH = 4;

    private int heading;

    public ShipHitEffect(Vector position, int heading) {
        this(position, heading, new Vector(0, 0));
    }

    public ShipHitEffect(Vector position, int heading, Vector velocity) {
        super(SPRITE_ID, SPRITE_SIZE, position,
                velocity, ANIMATION_LENGTH * 2);
        this.position = position;
        this.heading = heading;
        this.velocity = velocity;

        this.animationLength = ANIMATION_LENGTH;
        this.animationHorizontal = true;
        this.animationSpeed = 0.25;
    }

    @Override
    public void draw(GameMovieBuilder builder) {
        drawSprite(builder, 0, 0, 12, heading);
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