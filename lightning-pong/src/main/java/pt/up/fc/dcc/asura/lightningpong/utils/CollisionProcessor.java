package pt.up.fc.dcc.asura.lightningpong.utils;

/**
 * Utilities to detect and calculate collisions between objects
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class CollisionProcessor {

    public static boolean collidesRectCircle(double rx, double ry, double rw, double rh,
                                             double cx, double cy, double cr) {

        /*double distX = Math.abs(cx - rx);
        double distY = Math.abs(cy - ry);

        if (distX > (rw / 2 + cr))
            return false;

        if (distY > (rh / 2 + cr))
            return false;

        if (distX <= (rw / 2))
            return true;

        if (distY <= (rh / 2))
            return true;

        double dx = distX - rw / 2;
        double dy = distY - rh / 2;

        return dx * dx + dy * dy <= (cr * cr);*/


        // temporary variables to set edges for testing
        double testX = cx;
        double testY = cy;

        // which edge is closest?
        if (cx < rx - rw/2)         testX = rx - rw/2;      // test left edge
        else if (cx > rx+rw/2) testX = rx + rw/2;           // test right edge
        if (cy < ry - rh/2)    testY = ry - rh/2;           // test top edge
        else if (cy > ry+rh/2)   testY = ry + rh/2;         // test bottom edge

        // get distance from closest edges
        double distX = cx-testX;
        double distY = cy-testY;
        double distance = Math.sqrt( (distX*distX) + (distY*distY) );

        // if the distance is less than the radius, collision!
        if (distance <= cr) {
            return true;
        }
        return false;
    }

    public static boolean collidesCircleCircle(double c1x, double c1y, double c1r,
                                               double c2x, double c2y, double c2r) {

        double distX = Math.abs(c1x - c2x);
        double distY = Math.abs(c1y - c2y);

        return Math.sqrt(distX * distX + distY * distY) <= c1r + c2r;
    }

    public static Vector distanceRectCircle(double rx, double ry, double rw, double rh,
                                            double cx, double cy, double cr) {

        double nearestX = Math.max(
                rx - rw / 2,
                Math.min(
                        cx,
                        rx + rw / 2));

        double nearestY = Math.max(
                ry - rh / 2,
                Math.min(
                        cy,
                        ry + rh / 2));

        return new Vector(cx - nearestX, cy - nearestY);
    }

}
