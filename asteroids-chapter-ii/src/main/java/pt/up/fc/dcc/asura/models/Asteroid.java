package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.AsteroidsState;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.models.effects.AsteroidExplosionEffect;
import pt.up.fc.dcc.asura.models.effects.AsteroidHitEffect;
import pt.up.fc.dcc.asura.models.effects.EffectActor;
import pt.up.fc.dcc.asura.utils.Vector;

/**
 * Representation of an asteroid
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Asteroid extends NonPlayerAliveActor {
    private static final String SPRITE_ID_FORMAT = "a%d";
    private static final String[] SPRITES = {
            "asteroid1.png", "asteroid2.png", "asteroid3.png", "asteroid4.png"
    };
    private static final int ANIMATION_LENGTH = 180;
    private static final double ASTEROID_RADIUS = 8D;
    private static final double MAX_VELOCITY = 2D;

    private int size;
    private int type;

    public Asteroid(String spriteId, Vector position, Vector velocity, int size, int type) {
        super(spriteId, position, velocity);
        this.size = size;
        this.type = type;

        this.health = size;

        this.animationForward = Math.random() < 0.5D;
        this.animationSpeed = 0.3D + Math.random() * 0.5D;
        this.animationLength = ANIMATION_LENGTH;
    }

    public static Asteroid createAsteroid() {
        Vector position = new Vector(Math.random() * AsteroidsState.GAME_WORLD_WIDTH,
                Math.random() * AsteroidsState.GAME_WORLD_HEIGHT);
        Vector velocity = new Vector(
                Math.random() * MAX_VELOCITY + 0.2,
                Math.random() * MAX_VELOCITY + 0.2);
        return createAsteroid(position, velocity);
    }

    public static Asteroid createAsteroid(Vector position) {
        Vector velocity = new Vector(
                Math.random() * MAX_VELOCITY + 0.2,
                Math.random() * MAX_VELOCITY + 0.2);
        return createAsteroid(position, velocity);
    }

    public static Asteroid createAsteroid(Vector position, Vector velocity) {
        int size = (int) (Math.random() * 4) % 2 == 0 ? 4 : 3;
        return createAsteroid(position, velocity, size);
    }

    public static Asteroid createAsteroid(Vector position, Vector velocity, int size) {
        int type = (int) (Math.random() * SPRITES.length) + 1;
        return createAsteroid(position, velocity, size, type);
    }

    public static Asteroid createAsteroid(Vector position, Vector velocity, int size, int type) {
        return new Asteroid(String.format(SPRITE_ID_FORMAT, type - 1),
                position, velocity, size, type);
    }

    public int size() {
        return size;
    }

    public int type() {
        return type;
    }

    @Override
    public double radius() {
        return size * ASTEROID_RADIUS;
    }

    @Override
    public void onUpdate() {

    }

    public EffectActor getHitEffect(long time, Actor actor) {

        Vector effectVelocity = (Vector) velocity.clone();
        effectVelocity.scale(0.25);

        Vector hitActorVelocity = (Vector) actor.getVelocity().clone();
        hitActorVelocity.scale(0.01);

        effectVelocity.add(hitActorVelocity);

        return new AsteroidHitEffect(time, actor.getPosition(), (int) Math.toDegrees(effectVelocity.angle2d()),
                effectVelocity, size);
    }

    public EffectActor getExplosionEffect(long time, Actor actor) {

        Vector effectVelocity = (Vector) velocity.clone();
        effectVelocity.scale(0.15);

        Vector hitActorVelocity = (Vector) actor.getVelocity().clone();
        hitActorVelocity.scale(0.01);

        effectVelocity.add(hitActorVelocity);

        return new AsteroidExplosionEffect(time, position, (int) Math.toDegrees(effectVelocity.angle2d()),
                effectVelocity, size);
    }

    @Override
    public void draw(long time, GameMovieBuilder builder) {
        double radius = radius();
        drawSprite(builder, 0, 0, (int) radius * 2, 0);
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
