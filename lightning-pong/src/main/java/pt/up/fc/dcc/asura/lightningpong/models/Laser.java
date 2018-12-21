package pt.up.fc.dcc.asura.lightningpong.models;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.lightningpong.utils.CollisionProcessor;
import pt.up.fc.dcc.asura.lightningpong.utils.Sprite;
import pt.up.fc.dcc.asura.lightningpong.utils.SpriteManager;
import pt.up.fc.dcc.asura.lightningpong.utils.Vector;

/**
 * Laser fired by a player
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Laser extends Actor {
    private static final double SCALE = 0.15;
    private static final double DEFAULT_SPEED = 10;
    private static final long MAX_LIFETIME = 65;

    private Sprite sprite;

    private double radius;

    private long lifetime = MAX_LIFETIME;

    public Laser(Vector position, int order) {
        this(position, new Vector((order == 0 ? 1 : -1) * DEFAULT_SPEED, 0), order);
    }

    public Laser(Vector position, Vector velocity, int order) {
        super(position, velocity);
        this.sprite = SpriteManager.getSpriteFor(this, order)
            .scaleObject(SCALE);
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

    @Override
    public void onUpdate() {

        position = position.add(velocity);

        lifetime = Math.max(lifetime - 1, 0);
    }

    @Override
    public boolean onHit(Actor actor, double strength) {
        lifetime = 0;
        return true;
    }

    @Override
    public boolean expired() {
        return lifetime <= 0;
    }

    @Override
    public boolean collides(Actor actor) {

        if (actor instanceof Paddle)
            return CollisionProcessor.collidesRectCircle(
                    actor.getPosition().getX(), actor.getPosition().getY(),
                    actor.width(), actor.height(),
                    getPosition().getX(), getPosition().getY(), radius
            );
        else if (actor instanceof Ball || actor instanceof PowerUp)
            return CollisionProcessor.collidesCircleCircle(
                    getPosition().getX(), getPosition().getY(), radius,
                    actor.getPosition().getX(), actor.getPosition().getY(), actor.width()/2
            );

        return false;
    }

    @Override
    public void draw(GameMovieBuilder builder) {

        Sprite.AnimationFrame animFrame = sprite.getAnimationFrame((int) (MAX_LIFETIME - lifetime));

        builder.addItem(sprite.getName(),
                (int) getPosition().getX(), (int) getPosition().getY(),
                0, SCALE,
                animFrame.getX(), animFrame.getY(),
                animFrame.getWidth(), animFrame.getHeight());
    }
}
