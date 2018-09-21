package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.AsteroidsState;

/**
 * Base class for weapons of a ship.
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public abstract class Weapon {
    private static final int WEAPON_RECHARGE_TIME = 125;

    protected Ship ship;

    private long lastRechargeTime = 0;

    public Weapon(Ship ship) {
        this.ship = ship;
    }

    public Bullet fire(long time) {
        if (lastRechargeTime == 0
                || time - lastRechargeTime > weaponRechargeTime()) {
            lastRechargeTime = time;
            return doFire(time);
        }

        return null;
    }

    protected abstract Bullet doFire(long time);

    protected int weaponRechargeTime() {
        return WEAPON_RECHARGE_TIME;
    }
}
