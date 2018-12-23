package pt.up.fc.dcc.asura.snake.models;

import pt.up.fc.dcc.asura.snake.utils.Vector;

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
     * Actor game loop update event method.
     */
    public abstract void onUpdate();

    /**
     * Is this actor expired?
     *
     * @return {@code true} if it is expired, {@code false} otherwise
     */
    public boolean expired() {
        return false;
    }
}
