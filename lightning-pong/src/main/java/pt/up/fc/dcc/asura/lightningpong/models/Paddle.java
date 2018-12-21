package pt.up.fc.dcc.asura.lightningpong.models;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.lightningpong.LightningPongState;
import pt.up.fc.dcc.asura.lightningpong.utils.CollisionProcessor;
import pt.up.fc.dcc.asura.lightningpong.utils.Sprite;
import pt.up.fc.dcc.asura.lightningpong.utils.SpriteManager;
import pt.up.fc.dcc.asura.lightningpong.utils.Vector;

/**
 * Paddle with which the player hits the ball
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Paddle extends Actor {
    private static final double DEFAULT_LARGER_INCREASE = 1.5;
    private static final double MOVE_SPEED = 4;

    private static final long RECHARGING_TIME = 100;

    private int order;
    private Sprite sprite;

    private double width;
    private double height;

    private long recharging = RECHARGING_TIME;
    private long largerTime = 0;
    private long fasterTime = 0;
    private long frozenTime = 0;

    public Paddle(Vector position, Vector velocity, int order) {
        super(position, velocity);
        this.order = order;
        this.sprite = SpriteManager.getSpriteFor(this, order);
        this.height = this.sprite.getObjectHeight();
        this.width = this.sprite.getObjectWidth();
    }

    @Override
    public double width() {
        return width;
    }

    @Override
    public double height() {

        if (largerTime > 0)
            return DEFAULT_LARGER_INCREASE * height;

        return height;
    }

    public boolean up() {

        if (position.getY() <= height()/2)
            return false;

        int minY = (int) Math.max(position.getY() - MOVE_SPEED * (fasterTime > 0 ? 2 : 1),
                height()/2);
        position.setY(minY);
        return true;
    }

    public boolean down() {

        if (position.getY() >= LightningPongState.GAME_WORLD_HEIGHT - height()/2)
            return false;

        int maxY = (int) Math.min(position.getY() + MOVE_SPEED * (fasterTime > 0 ? 2 : 1),
                LightningPongState.GAME_WORLD_HEIGHT - height()/2);
        position.setY(maxY);
        return true;
    }

    public boolean right() {

        if (position.getX() >= LightningPongState.GAME_WORLD_WIDTH - width()/2)
            return false;

        int maxX = (int) Math.min(position.getX() + MOVE_SPEED * (fasterTime > 0 ? 2 : 1),
                LightningPongState.GAME_WORLD_WIDTH - width()/2);
        position.setX(maxX);
        return true;
    }

    public boolean left() {

        if (position.getX() <= width()/2)
            return false;

        int minX = (int) Math.max(position.getX() - MOVE_SPEED * (fasterTime > 0 ? 2 : 1),
                width()/2);
        position.setX(minX);
        return true;
    }

    public Laser fire() {

        if (recharging > 0 || frozenTime > 0)
            return null;

        recharging = RECHARGING_TIME;

        return new Laser(
                new Vector(
                        position.getX() + (width()/2 + 0.1) * (order == 0 ? 1 : -1),
                        position.getY()),
                order);
    }

    public void freeze(long time) {
        this.frozenTime = time;
    }

    public long getFrozenTime() {
        return frozenTime;
    }

    public void setLarger(long largerTime) {
        this.largerTime = largerTime;
    }

    public long getLargerTime() {
        return largerTime;
    }

    public void setFaster(long fasterTime) {
        this.fasterTime = fasterTime;
    }

    public long getFasterTime() {
        return fasterTime;
    }

    @Override
    public void onUpdate() {

        if (recharging > 0) recharging--;
        if (fasterTime > 0) fasterTime--;
        if (largerTime > 0) largerTime--;
        if (frozenTime > 0) frozenTime--;
    }

    @Override
    public boolean collides(Actor actor) {

        if (actor instanceof Ball || actor instanceof Laser || actor instanceof PowerUp)
            return CollisionProcessor.collidesRectCircle(
                    getPosition().getX(), getPosition().getY(), width(), height(),
                    actor.getPosition().getX(), actor.getPosition().getY(), actor.width()/2);

        return false;
    }

    @Override
    public void draw(GameMovieBuilder builder) {
        builder.addItem(sprite.getName(),
                (int) position.getX(), (int) position.getY(),
                0, largerTime > 0 ? DEFAULT_LARGER_INCREASE : 1);
    }
}
