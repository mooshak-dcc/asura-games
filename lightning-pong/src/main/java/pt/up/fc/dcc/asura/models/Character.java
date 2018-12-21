package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.utils.Sprite;
import pt.up.fc.dcc.asura.utils.SpriteManager;
import pt.up.fc.dcc.asura.utils.Vector;

/**
 * Show a character in the screen
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Character extends Actor {

    private Sprite sprite;
    private int animFrameIdx;

    private double width;
    private double height;

    private long lifetime;
    private double scale;
    private double rotate;


    public Character(Vector position, int animFrameIdx) {
        this(position, animFrameIdx, -1);
    }

    public Character(Vector position, int animFrameIdx, long lifetime) {
        this(position, animFrameIdx, lifetime, 1);
    }

    public Character(Vector position, int animFrameIdx, long lifetime, double scale) {
        this(position, animFrameIdx, lifetime, scale, 0);
    }

    public Character(Vector position, int animFrameIdx, long lifetime, double scale, double rotate) {
        super(position, new Vector(0, 0));
        this.sprite = SpriteManager.getSpriteFor(this);
        this.animFrameIdx = animFrameIdx;
        this.width = this.sprite.getObjectWidth();
        this.height = this.sprite.getObjectHeight();
        this.scale = scale;
        this.rotate = rotate;
    }

    @Override
    public double width() {
        return width;
    }

    @Override
    public double height() {
        return height;
    }

    @Override
    public void onUpdate() {

        if (lifetime > 0) lifetime--;
    }

    @Override
    public boolean expired() {
        return lifetime == 0;
    }

    @Override
    public boolean collides(Actor actor) {
        return false;
    }

    @Override
    public void draw(GameMovieBuilder builder) {

        Sprite.AnimationFrame animFrame = sprite.getAnimationFrame(animFrameIdx);

        builder.addItem(sprite.getName(),
                (int) getPosition().getX(), (int) getPosition().getY(),
                rotate, scale,
                animFrame.getX(), animFrame.getY(),
                animFrame.getWidth(), animFrame.getHeight());
    }
}
