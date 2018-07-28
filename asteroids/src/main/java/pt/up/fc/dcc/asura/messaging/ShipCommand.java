package pt.up.fc.dcc.asura.messaging;

/**
 * Command sent from a player
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class ShipCommand {
    private boolean thrust;
    private boolean steerRight;
    private boolean steerLeft;
    private boolean shield;
    private int fire;

    public ShipCommand() {
    }

    public ShipCommand(boolean thrust, boolean steerLeft, boolean steerRight, boolean shield, int fire) {
        this.thrust = thrust;
        this.steerLeft = steerLeft;
        this.steerRight = steerRight;
        this.shield = shield;
        this.fire = fire;
    }

    public boolean isThrust() {
        return thrust;
    }

    public void setThrust(boolean thrust) {
        this.thrust = thrust;
    }

    public boolean isSteerRight() {
        return steerRight;
    }

    public void setSteerRight(boolean steerRight) {
        this.steerRight = steerRight;
    }

    public boolean isSteerLeft() {
        return steerLeft;
    }

    public void setSteerLeft(boolean steerLeft) {
        this.steerLeft = steerLeft;
    }

    public boolean isShield() {
        return shield;
    }

    public void setShield(boolean shield) {
        this.shield = shield;
    }

    public int getFire() {
        return fire;
    }

    public void setFire(int fire) {
        this.fire = fire;
    }
}
