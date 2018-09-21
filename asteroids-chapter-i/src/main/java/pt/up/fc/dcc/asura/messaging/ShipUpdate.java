package pt.up.fc.dcc.asura.messaging;

import java.util.ArrayList;
import java.util.List;

public class ShipUpdate {
    private int x;
    private int y;
    private double velocity;
    private double heading;
    private int health;
    private double energy;
    private int shieldCounter;
    private List<int[]> asteroids = new ArrayList<>();

    public ShipUpdate() {
    }

    public ShipUpdate(int x, int y, int heading, int health, int energy, int shieldCounter) {
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.health = health;
        this.energy = energy;
        this.shieldCounter = shieldCounter;
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

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public int getShieldCounter() {
        return shieldCounter;
    }

    public void setShieldCounter(int shieldCounter) {
        this.shieldCounter = shieldCounter;
    }

    public void addAsteroid(int x, int y) {
        asteroids.add(new int[] {x,y});
    }

    public List<int[]> getAsteroids() {
        return asteroids;
    }
}
