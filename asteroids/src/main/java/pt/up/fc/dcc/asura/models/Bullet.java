package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.AsteroidsState;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.utils.Vector;

/**
 * Base bullet clas
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Bullet extends Actor {
    private static final String SPRITE_ID_FORMAT = "b%d";
    private static final String[] SPRITES = {
            "laser1.png"/*, "bullet2.png", "bullet3.png", "bullet4.png"*/
    };
    private static final int SPRITE_WIDTH = 55;
    private static final int SPRITE_HEIGHT = 110;
    private static final int BULLET_WIDTH = 3;
    private static final int BULLET_HEIGHT = 6;
    private static final int BULLET_LIFESPAN = 400;
    private static final int BULLET_POWER = 1;

    protected String spriteId;
    protected int teamNr;

    protected int heading;
    protected int lifespan;
    protected int startTime;
    protected int power;

    private int health = 1;

    public Bullet(int teamNr, Vector position, Vector velocity, int heading) {
        this(teamNr, position, velocity, heading, BULLET_LIFESPAN, BULLET_POWER);
    }

    public Bullet(int teamNr, Vector position, Vector velocity, int heading, int lifespan) {
        this(teamNr, position, velocity, heading, BULLET_LIFESPAN, BULLET_POWER);
    }

    public Bullet(int teamNr, Vector position, Vector velocity, int heading, int lifespan, int power) {
        super(position, velocity);
        this.spriteId = String.format(SPRITE_ID_FORMAT, teamNr % SPRITES.length);
        this.teamNr = teamNr;
        this.heading = heading;
        this.lifespan = lifespan;
        this.startTime = AsteroidsState.time;
        this.power = power;
    }

    public int heading() {
        return heading;
    }

    public double effectRadius() {
        return 0;
    }

    public int power() {
        return power;
    }

    public boolean hit(int force) {

        if (force >= 0)
            health -= force;
        else
            health = 0;

        return health <= 0;
    }

    public EffectActor getHitEffect(Actor actor) {
        Vector effectVelocity = (Vector) velocity.clone();

        if (actor instanceof Asteroid) {
            effectVelocity.scale(0.05);
            return new BulletHitEffect(teamNr, position, heading, effectVelocity);
        } else if (actor instanceof Ship) {
            effectVelocity.scale(0.1);
            return new BulletHitEffect(teamNr, position, heading, effectVelocity);
        }

        return null;
    }

    @Override
    public double radius() {
        return (BULLET_WIDTH + BULLET_HEIGHT) / 2;
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public boolean expired() {
        return health <= 0 || (AsteroidsState.time - startTime) > lifespan;
    }

    @Override
    public void draw(GameMovieBuilder builder) {

        builder.addItem(spriteId,
                (int) position.getX(), (int) position.getY(),
                Math.toRadians(heading),(double) BULLET_WIDTH / SPRITE_WIDTH);
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
