package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.AsteroidsState;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.utils.Vector;

/**
 * An actor that has no influence in the game. It is just an design effect.
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public abstract class EffectActor extends DrawableActor {

    protected int startTime;
    protected int lifespan;

    public EffectActor(String spriteId, int spriteSize, Vector position, int lifespan) {
        this(spriteId, spriteSize, position, new Vector(0, 0), lifespan);
    }

    public EffectActor(String spriteId, int spriteSize, Vector position, Vector velocity,
                       int lifespan) {
        super(spriteId, spriteSize, position, velocity);
        this.startTime = AsteroidsState.time;
        this.lifespan = lifespan;
    }

    @Override
    public double radius() {
        return 0;
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public boolean expired() {
        return (AsteroidsState.time - startTime) >= lifespan;
    }
}
