package pt.up.fc.dcc.asura.snake.utils;

public class Vector implements Cloneable {

    private int x;
    private int y;

    public Vector(int x, int y) {
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

    public Vector add(Vector v) {
        return new Vector(getX() + v.getX(), getY() + v.getY());
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return new Vector(x, y);
        }
    }
}
