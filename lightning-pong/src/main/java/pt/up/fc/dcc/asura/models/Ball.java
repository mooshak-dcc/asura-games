package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.LightningPongState;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.utils.CollisionProcessor;
import pt.up.fc.dcc.asura.utils.Sprite;
import pt.up.fc.dcc.asura.utils.SpriteManager;
import pt.up.fc.dcc.asura.utils.Vector;

/**
 * Ball of the game
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Ball extends Actor {
    private static final double DEFAULT_SPEED = 5;

    private Sprite sprite;

    private double radius;
    private long fasterTime = 0;
    private long frozenTime = 0;

    public Ball(Vector position) {
        this(position, Math.random() > 0.5);
    }

    public Ball(Vector position, boolean direction) {
        this(position, (direction ?
                Math.random() * (1D/4D - (-1D/4D)) - 1D/4D :
                Math.random() * (5D/4D - 3D/4D) + 3D/4D) * Math.PI);
    }

    private Ball(Vector position, double theta) {
        this(position, new Vector(DEFAULT_SPEED, 0).rotate2d(theta));
    }

    public Ball(Vector position, Vector velocity) {
        super(position, velocity);
        this.sprite = SpriteManager.getSpriteFor(this);
        this.radius = (double) this.sprite.getObjectWidth() / 2;
    }

    @Override
    public double width() {
        return radius * 2;
    }

    @Override
    public double height() {
        return radius * 2;
    }

    public void freeze(long time) {
        this.frozenTime = time;
    }

    public long getFrozenTime() {
        return frozenTime;
    }

    public void setFaster(long fasterTime) {
        this.fasterTime = fasterTime;
    }

    public long getFasterTime() {
        return fasterTime;
    }

    @Override
    public void onUpdate() {

        if (frozenTime <= 0) {

            if (fasterTime > 0)
                position = position.add(velocity.scale(2));
            else
                position = position.add(velocity);

            // handle traversing out of the board
            if ((position.getY() + radius) >= LightningPongState.GAME_WORLD_HEIGHT) {
                velocity.setY(- velocity.getY());
                position.setY(LightningPongState.GAME_WORLD_HEIGHT - radius);
            } else if ((position.getY() - radius) <= 0) {
                velocity.setY(- velocity.getY());
                position.setY(radius);
            }
        } else
            frozenTime--;

        if (fasterTime > 0) fasterTime--;
    }

    @Override
    public boolean collides(Actor actor) {

        if (actor instanceof Paddle)
            return CollisionProcessor.collidesRectCircle(
                    actor.getPosition().getX(), actor.getPosition().getY(),
                    actor.width(), actor.height(),
                    getPosition().getX(), getPosition().getY(), radius
            );
        else if (actor instanceof Laser)
            return CollisionProcessor.collidesCircleCircle(
                    getPosition().getX(), getPosition().getY(), radius,
                    actor.getPosition().getX(), actor.getPosition().getY(),
                    actor.width() / 2
            );

        return false;
    }

    @Override
    public void draw(GameMovieBuilder builder) {
        builder.addItem(sprite.getName(), (int) getPosition().getX(),
                (int) getPosition().getY());
    }
}
