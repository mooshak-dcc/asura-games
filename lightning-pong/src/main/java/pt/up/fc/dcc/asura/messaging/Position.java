package pt.up.fc.dcc.asura.messaging;

/**
 * Update on the position of an object
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Position {

    private int x;
    private int y;

    public Position() {
    }

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
