package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.utils.Vector;

/**
 * Actors have a position in the game world and a velocity vector of travel per frame.
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public abstract class Actor implements Drawable {

    protected Vector position;
    protected Vector velocity;

    public Actor(Vector position, Vector velocity) {
        this.position = position;
        this.velocity = velocity;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public Vector getPosition() {
        return position;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    public Vector getVelocity() {
        return velocity;
    }

    /**
     * Radius of the actor
     *
     * @return {@code double} radius
     */
    public abstract double radius();

    /**
     * Actor game loop update event method.
     */
    public abstract void onUpdate();

    /**
     * Check whether an actor collides with another actor
     *
     * @param actor {@link Actor} the other actor
     * @return {@code true} if the actor collides with the other, {@code false} otherwise
     */
    public boolean collides(Actor actor) {

        return position.distance(actor.getPosition()) <= radius() + actor.radius();
    }

    /**
     * Checks whether an actor is in a given range of another actor
     *
     * @param actor {@link Actor} the other actor
     * @return {@code true} if the actor collides with the other, {@code false} otherwise
     */
    public boolean withinRange(Actor actor, double range) {

        return position.distance(actor.getPosition()) <= radius() + actor.radius() + range;
    }

    /**
     * Is this actor expired?
     *
     * @param time {@code long} time
     * @return {@code true} if it is expired, {@code false} otherwise
     */
    public boolean expired(long time) {
        return false;
    }
}
