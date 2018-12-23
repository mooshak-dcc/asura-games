package pt.up.fc.dcc.asura.snake.utils;

import static pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder.SpriteAnchor;

/**
 * Representation of a game sprite
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Sprite {

    private String name;
    private String extension;

    private SpriteAnchor anchor;

    private int width;
    private int height;

    private int objectWidth;
    private int objectHeight;

    private int animationWidth;
    private int animationHeight;

    public Sprite(String name, String extension, SpriteAnchor anchor, int width, int height) {
        this(name, extension, anchor, width, height, width, height);
    }

    public Sprite(String name, String extension, SpriteAnchor anchor, int width, int height,
                  int objectWidth, int objectHeight) {
        this(name, extension, anchor, width, height, objectWidth, objectHeight,
                width, height);
    }

    public Sprite(String name, String extension, SpriteAnchor anchor, int width, int height,
                  int objectWidth, int objectHeight,
                  int animationWidth, int animationHeight) {
        this.name = name;
        this.extension = extension;
        this.anchor = anchor;
        this.width = width;
        this.height = height;
        this.objectWidth = objectWidth;
        this.objectHeight = objectHeight;
        this.animationWidth = animationWidth;
        this.animationHeight = animationHeight;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public String getFilename() {
        return String.format("%s.%s", name, extension);
    }

    public SpriteAnchor getAnchor() {
        return anchor;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getObjectWidth() {
        return objectWidth;
    }

    public int getObjectHeight() {
        return objectHeight;
    }

    public int getAnimationWidth() {
        return animationWidth;
    }

    public int getAnimationHeight() {
        return animationHeight;
    }

    public Sprite scaleObject(double s) {

        return new Sprite(name, extension, anchor,
                width, height,
                (int) (objectWidth * s), (int) (objectHeight * s),
                animationWidth, animationHeight);
    }

    private Integer horizontalAnimationFrames;
    private Integer verticalAnimationFrames;

    public AnimationFrame getAnimationFrame(int frame) {

        if (horizontalAnimationFrames == null || verticalAnimationFrames == null) {
            horizontalAnimationFrames = (int) Math.max(Math.ceil((double) width / animationWidth), 1);
            verticalAnimationFrames = (int) Math.max(Math.ceil((double) height / animationHeight), 1);
        }

        frame = frame % (horizontalAnimationFrames * verticalAnimationFrames);

        int vFrameOffset = frame / horizontalAnimationFrames;
        int hFrameOffset = frame % horizontalAnimationFrames;

        return new AnimationFrame(
                animationWidth * hFrameOffset, animationHeight * vFrameOffset,
                animationWidth, animationHeight);
    }

    public static class AnimationFrame {

        private int x;
        private int y;

        private int width;
        private int height;

        public AnimationFrame(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
