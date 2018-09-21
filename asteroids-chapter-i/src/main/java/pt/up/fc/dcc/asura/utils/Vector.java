package pt.up.fc.dcc.asura.utils;

public class Vector implements Cloneable {

    private double x;
    private double y;
    private double z;

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    public Vector(double x, double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Double getZ() {
        return z;
    }

    public double length() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    public void add(Vector v) {
        x += v.getX();
        y += v.getY();
        z += v.getZ();
    }

    public void scale(double scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
    }

    public void rotate2d(double theta) {

        double newX = x * Math.cos(theta) - y * Math.sin(theta);
        double newY = x * Math.sin(theta) + y * Math.cos(theta);

        x = newX;
        y = newY;
    }

    public void rotate(Vector t, double theta) {

        double u = t.getX(), v = t.getY(), w = t.getZ();

        double xPrime = u * (u * x + v * y + w * z) * (1D - Math.cos(theta))
                + x * Math.cos(theta)
                + (-w * y + v * z) * Math.sin(theta);
        double yPrime = v * (u * x + v * y + w * z) * (1D - Math.cos(theta))
                + y * Math.cos(theta)
                + (w * x - u * z) * Math.sin(theta);
        double zPrime = w * (u * x + v * y + w * z) * (1D - Math.cos(theta))
                + z * Math.cos(theta)
                + (-v * x + u * y) * Math.sin(theta);

        x = xPrime;
        y = yPrime;
        z = zPrime;
    }

    public double distance(Vector t) {
        return Math.sqrt(
                        Math.pow(x - t.getX(), 2) +
                        Math.pow(y - t.getY(), 2) +
                        Math.pow(z - t.getZ(), 2));
    }

    public double angle2d() {
        return Math.atan2(y, x);
    }

    public double headingTo(Vector t) {

        return Math.atan2((getY() - t.getY()), (getX() - t.getX()));
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return new Vector(x, y, z);
        }
    }
}
