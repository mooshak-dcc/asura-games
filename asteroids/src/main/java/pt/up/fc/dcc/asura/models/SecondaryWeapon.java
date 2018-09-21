package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.utils.Vector;

public class SecondaryWeapon extends Weapon {
    private static final double BULLET_SPEED = -1D;

    public SecondaryWeapon(Ship ship) {
        super(ship);
    }

    @Override
    protected Bullet doFire(long time) {
        Vector velocity = new Vector(0D, BULLET_SPEED);
        velocity.rotate2d(Math.toRadians(ship.getHeading()));
        velocity.add(ship.getVelocity());

        Vector position = (Vector) ship.getPosition().clone();
        Vector addToPos = new Vector(0, -(ship.radius() * 2 + 1));
        addToPos.rotate2d(Math.toRadians(ship.getHeading()));
        position.add(addToPos);

        return new Bomb(ship.getPlayerId(), ship.getTeamNr(), time, ship.getFireCount(), position, velocity,
                ship.getHeading());
    }
}
