package pt.up.fc.dcc.asura.lightningpong.models;

import pt.up.fc.dcc.asura.lightningpong.utils.Vector;

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
     * Width of the actor
     *
     * @return {@code double} width
     */
    public abstract double width();

    /**
     * Height of the actor
     *
     * @return {@code double} height
     */
    public abstract double height();

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
    public abstract boolean collides(Actor actor);

    /**
     * Is this actor expired?
     *
     * @return {@code true} if it is expired, {@code false} otherwise
     */
    public boolean expired() {
        return false;
    }

    /**
     * Actor was hit by another actor
     *
     * @param actor {@link Actor} actor that inflicted damage
     * @param strength {@code double} strength of the hit
     * @return {@code true} if the actor died, {@code false} otherwise
     */
    public boolean onHit(Actor actor, double strength) {
        return false;
    }
}
