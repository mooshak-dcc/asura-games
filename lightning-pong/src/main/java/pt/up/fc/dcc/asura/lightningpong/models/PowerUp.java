package pt.up.fc.dcc.asura.lightningpong.models;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.lightningpong.LightningPongState;
import pt.up.fc.dcc.asura.lightningpong.utils.CollisionProcessor;
import pt.up.fc.dcc.asura.lightningpong.utils.Sprite;
import pt.up.fc.dcc.asura.lightningpong.utils.SpriteManager;
import pt.up.fc.dcc.asura.lightningpong.utils.Vector;

/**
 * Power-up that travels the game world at random velocity
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class PowerUp extends Actor {
    private static final double SCALE = 0.25;
    private static final long MAX_LIFETIME = 500;
    private static final double MAX_SPEED = 6;
    private static final double MIN_SPEED = 2;

    private Sprite sprite;

    private long lifetime = MAX_LIFETIME;
    private Bonus bonus;
    private double radius;

    public PowerUp(int order) {
        super(
                new Vector(
                        Math.random() * (
                                (LightningPongState.GAME_WORLD_WIDTH * 3 >> 2) - (LightningPongState.GAME_WORLD_WIDTH >> 2)) +
                                (LightningPongState.GAME_WORLD_WIDTH >> 2),
                        Math.random() * LightningPongState.GAME_WORLD_HEIGHT),
                new Vector(Math.random() * (MAX_SPEED - MIN_SPEED) + MIN_SPEED, 0)
                        .rotate2d(Math.random() * 2 * Math.PI));
        this.bonus = Bonus.values()[order];
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

    public Bonus getBonus() {
        return bonus;
    }

    @Override
    public boolean expired() {
        return lifetime <= 0;
    }

    @Override
    public void onUpdate() {

        position = position.add(velocity);

        // handle traversing out of the board
        if ((position.getY() + radius) >= LightningPongState.GAME_WORLD_HEIGHT) {
            velocity.setY(- velocity.getY());
            position.setY(LightningPongState.GAME_WORLD_HEIGHT - radius);
        } else if ((position.getY() - radius) <= 0) {
            velocity.setY(- velocity.getY());
            position.setY(radius);
        }

        if ((position.getX() - radius) >= LightningPongState.GAME_WORLD_WIDTH)
            lifetime = 0;
        else if ((position.getX() + radius) <= 0)
            lifetime = 0;

        lifetime--;
    }

    @Override
    public boolean onHit(Actor actor, double strength) {
        lifetime = 0;
        return true;
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

        Sprite.AnimationFrame animFrame = sprite.getAnimationFrame((int) (MAX_LIFETIME - lifetime));

        builder.addItem(sprite.getName(),
                (int) getPosition().getX(), (int) getPosition().getY(),
                0, SCALE,
                animFrame.getX(), animFrame.getY(),
                animFrame.getWidth(), animFrame.getHeight());
    }
}
