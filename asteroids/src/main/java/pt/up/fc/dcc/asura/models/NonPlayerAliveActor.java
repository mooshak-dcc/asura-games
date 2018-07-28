package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.utils.Vector;

/**
 * An actor is that can be hit and destroyed by player bullets.
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public abstract class NonPlayerAliveActor extends DrawableActor {

    protected int health = 0;

    public NonPlayerAliveActor(String spriteId, Vector position, Vector velocity) {
        super(spriteId, position, velocity);
    }

    public boolean hit(double force) {

        if (force >= 0)
            health -= force;
        else
            health = 0;

        return health <= 0;
    }

    @Override
    public boolean expired() {
        return health <= 0;
    }
}
