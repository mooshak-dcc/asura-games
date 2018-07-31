package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.AsteroidsState;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.models.effects.BombExplosionEffect;
import pt.up.fc.dcc.asura.models.effects.EffectActor;
import pt.up.fc.dcc.asura.utils.Vector;

public class Bomb extends Bullet {
    private static final String SPRITE_ID_FORMAT = "b%d";
    private static final String[] SPRITES = {
            "b1.png", "b2.png", "b3.png", "b4.png"
    };
    private static final int SPRITE_SIZE = 256;
    private static final int BOMB_WIDTH = 20;
    private static final int BOMB_HEIGHT = 20;
    private static final int BOMB_LIFESPAN = 500;
    private static final int BOMB_POWER = 4;

    public Bomb(String playerId, int teamNr, int bulletCount, Vector position, Vector velocity, int heading) {
        super(playerId, String.format(SPRITE_ID_FORMAT, teamNr % SPRITES.length), teamNr, bulletCount, position,
                velocity, heading, BOMB_LIFESPAN, BOMB_POWER);
    }

    @Override
    public double effectRadius() {
        return 50;
    }

    @Override
    public double radius() {
        return (BOMB_WIDTH + BOMB_HEIGHT) / 2;
    }

    public boolean exploded() {
        return health <= 0;
    }

    @Override
    public EffectActor getHitEffect(Actor actor) {
        return new BombExplosionEffect(teamNr, position, heading, new Vector(0,0));
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

        int offset = (AsteroidsState.time - startTime) % 3;
        builder.addItem(spriteId,
                (int) position.getX(), (int) position.getY(),
                Math.toRadians(heading + (AsteroidsState.time - startTime) * 2),
                (double) BOMB_WIDTH / SPRITE_SIZE,
                offset * SPRITE_SIZE, 0, SPRITE_SIZE, SPRITE_SIZE);
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
