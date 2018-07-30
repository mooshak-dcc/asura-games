package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.AsteroidsState;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.utils.Vector;

/**
 * Base bullet class
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Bullet extends Actor {
    private static final String SPRITE_ID_FORMAT = "l%d";
    private static final String[] SPRITES = {
            "l1.png", "l2.png", "l3.png", "l4.png"
    };
    private static final int SPRITE_WIDTH = 40;
    private static final int SPRITE_HEIGHT = 70;
    private static final int BULLET_WIDTH = 4;
    private static final int BULLET_HEIGHT = 7;
    private static final String BULLET_ID_FORMAT = "bullet%d";
    private static final int BULLET_LIFESPAN = 400;
    private static final int BULLET_POWER = 1;

    protected String spriteId;
    protected int teamNr;

    private String playerId;

    private String bulletId;

    protected int heading;
    protected int lifespan;
    protected int startTime;
    protected int power;

    protected int health = 1;

    private BulletResult result;

    public Bullet(String playerId, int teamNr, int bulletCount, Vector position, Vector velocity, int heading) {
        this(playerId, teamNr, bulletCount, position, velocity, heading, BULLET_LIFESPAN, BULLET_POWER);
    }

    public Bullet(String playerId, int teamNr, int bulletCount, Vector position, Vector velocity, int heading,
                  int lifespan) {
        this(playerId, teamNr, bulletCount, position, velocity, heading, lifespan, BULLET_POWER);
    }

    public Bullet(String playerId, int teamNr, int bulletCount, Vector position, Vector velocity, int heading,
                  int lifespan, int power) {
        this(playerId, String.format(SPRITE_ID_FORMAT, teamNr % SPRITES.length), teamNr, bulletCount, position,
                velocity, heading, lifespan, power);
    }

    public Bullet(String playerId, String spriteId, int teamNr, int bulletCount, Vector position, Vector velocity,
                  int heading, int lifespan, int power) {
        super(position, velocity);
        this.playerId = playerId;
        this.spriteId = spriteId;
        this.teamNr = teamNr;
        this.bulletId = String.format(BULLET_ID_FORMAT, bulletCount);
        this.heading = heading;
        this.lifespan = lifespan;
        this.startTime = AsteroidsState.time;
        this.power = power;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getBulletId() {
        return bulletId;
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

    public void setResult(BulletResult result) {
        this.result = result;
    }

    public BulletResult getResult() {
        return result;
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
