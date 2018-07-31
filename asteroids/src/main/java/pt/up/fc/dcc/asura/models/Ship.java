package pt.up.fc.dcc.asura.models;

import pt.up.fc.dcc.asura.AsteroidsState;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.models.effects.EffectActor;
import pt.up.fc.dcc.asura.models.effects.ShipExplosionEffect;
import pt.up.fc.dcc.asura.models.effects.ShipHitEffect;
import pt.up.fc.dcc.asura.utils.Vector;

import java.util.List;

/**
 * Representation of a ship
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Ship extends DrawableActor implements HasTeam {
    private static final String SPRITE_ID_FORMAT = "s%d";
    private static final String SHIELD_SPRITE_ID_FORMAT = "p%d";
    private static final String[] SPRITES = {
            "s1.png", "s2.png", "s3.png", "s4.png"
    };
    private static final String[] SHIELD_SPRITES = {
            "shield1.png", "shield2.png", "shield3.png", "shield4.png",
    };
    private static final int SPRITE_SIZE = 512;

    private static final double SHIP_RADIUS = 20D;
    private static final double SHIELD_RADIUS = 25D;

    private static final int STEER_STEP = 4;

    private static final int THRUST_INTERVAL = 5;
    private static final double THRUST_POWER = 0.5D;

    private static final int MIN_SHIELD_TIME = 20;
    private static final double SHIELD_ENERGY_DECREASE = 0.5D;
    private static final double SHIELD_INCREASE_FRAME = 0.1D;

    private static final double BOMB_ENERGY_DECREASE = 50;

    private static final double MAX_SPEED = 5D;
    public static final double MAX_ENERGY = 100D;
    public static final int MAX_HEALTH = 100;

    private static final int HIT_FORCE_MULTIPLIER = 10;

    private static int teamCount = 0;

    private String playerId;
    private String spriteId;
    private int teamNr;

    // rotation of the ship
    private int heading;

    // how many energy it still has (required for launching rockets & activate shield)
    private double energy = MAX_ENERGY;
    // how many health this ship has
    private int health = MAX_HEALTH;

    // when shield is activated
    private int shieldActiveCounter = 0;

    // recharges
    private int lastThrustTime = 0;

    // weapons
    private Weapon primaryWeapon;
    private Weapon secondaryWeapon;

    // a bullet counter (so we can identify bullets to notify about the result)
    private int fireCount = 0;

    // score of the ship / player
    private int score = 0;

    // frame at which player was killed
    private int killedOn = 0;

    private Ship(String playerId, String spriteId, String shieldSpriteId, int teamNr,
                 Vector position, Vector velocity, int heading) {
        super(shieldSpriteId, position, velocity);
        this.playerId = playerId;
        this.spriteId = spriteId;
        this.teamNr = teamNr;
        this.position = position;
        this.heading = heading;
        this.primaryWeapon = new PrimaryWeapon(this);
        this.secondaryWeapon = new SecondaryWeapon(this);
    }

    public static Ship create(String playerId) {
        return create(playerId, new Vector(0D, 0D), 0);
    }

    public static Ship create(String playerId, Vector position, int heading) {
        return create(playerId, position, new Vector(0D, 0D), heading);
    }

    public static Ship create(String playerId, Vector position, Vector velocity, int heading) {
        int teamNr = teamCount++ % SPRITES.length;
        return new Ship(playerId, String.format(SPRITE_ID_FORMAT, teamNr),
                String.format(SHIELD_SPRITE_ID_FORMAT, teamNr),
                teamNr, position, velocity, heading);
    }

    public String getPlayerId() {
        return playerId;
    }

    @Override
    public int getTeamNr() {
        return teamNr;
    }

    public int getFireCount() {
        return fireCount;
    }

    public int getScore() {
        return score;
    }

    public int getHeading() {
        return heading;
    }

    public int getHealth() {
        return health;
    }

    public double getEnergy() {
        return energy;
    }

    public int getShieldActiveCounter() {
        return shieldActiveCounter;
    }

    public int getKilledOn() {
        return killedOn;
    }

    public void addScorePoints(int points) {
        score += points;
    }

    public void thrust() {

        if (lastThrustTime == 0 || AsteroidsState.time - lastThrustTime > THRUST_INTERVAL) {

            Vector thrust = new Vector(0D, -THRUST_POWER);

            thrust.rotate2d(Math.toRadians(heading));

            velocity.add(thrust);

            if (velocity.length() > MAX_SPEED)
                velocity.scale(MAX_SPEED / velocity.length());

            lastThrustTime = AsteroidsState.time;
        }
    }

    public void steerLeft() {
        heading -= STEER_STEP;

        if (heading < 0)
            heading += 360;
    }

    public void steerRight() {
        heading += STEER_STEP;

        if (heading >= 360)
            heading -= 360;
    }

    public void activateShield() {
        if (energy >= MIN_SHIELD_TIME)
            shieldActiveCounter = MIN_SHIELD_TIME;
    }

    public boolean isShieldActive() {
        return shieldActiveCounter > 0 && energy > 0;
    }

    public void kill() {
        health = 0;
        killedOn = AsteroidsState.time;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void firePrimary(List<Bullet> bullets) {

        if (isAlive() && !isShieldActive()) {
            Bullet b = primaryWeapon.fire();
            if (b != null) {
                bullets.add(b);
            } else { // hack: add expired bullet so that bullet result is sent to player
                b = new Bullet(playerId, teamNr, fireCount, new Vector(-1, -1), new Vector(-1, -1), 0);
                b.hit(-1);
                b.setResult(BulletResult.WEAPON_LOCKED);
                bullets.add(b);
            }
        }

        fireCount++;
    }

    public void fireSecondary(List<Bomb> bombs) {

        if (isAlive() && !isShieldActive() && energy >= BOMB_ENERGY_DECREASE) {
            Bomb b = (Bomb) secondaryWeapon.fire();
            if (b != null) {
                bombs.add(b);
                energy -= BOMB_ENERGY_DECREASE;
            }
        }

        fireCount++;
    }

    public boolean hit(double force) {

        if (force >= 0)
            health -= force * HIT_FORCE_MULTIPLIER;
        else
            health = 0;

        if (health <= 0)
            kill();

        return health <= 0;
    }

    public EffectActor getHitEffect(Actor actor) {
        Vector effectVelocity = (Vector) actor.getVelocity().clone();
        effectVelocity.scale(0.1);

        Vector shipVelocity = (Vector) getVelocity().clone();
        shipVelocity.scale(0.7);

        effectVelocity.add(shipVelocity);

        return new ShipHitEffect(position, heading, effectVelocity);
    }

    public EffectActor getExplosionEffect(Actor actor) {

        Vector effectVelocity = (Vector) actor.getVelocity().clone();
        effectVelocity.scale(0.2);

        Vector shipVelocity = (Vector) getVelocity().clone();
        shipVelocity.scale(0.3);

        effectVelocity.add(shipVelocity);

        return new ShipExplosionEffect(position, heading, effectVelocity);
    }

    @Override
    public double radius() {
        return isShieldActive() ? SHIELD_RADIUS : SHIP_RADIUS;
    }

    @Override
    public void onUpdate() {

        if (energy <= 0)
            shieldActiveCounter = 0;

        if (!isShieldActive() && energy < MAX_ENERGY) // recharge shield if not active
            energy = Math.min(energy + SHIELD_INCREASE_FRAME, MAX_ENERGY);
        else if (isShieldActive()) { // decrease energy if shield is active
            shieldActiveCounter--;
            energy -= SHIELD_ENERGY_DECREASE;
        }
    }

    @Override
    public void draw(GameMovieBuilder builder) {

        double size = (SHIP_RADIUS * 2);

        double normAngle = Math.floor(heading - 1) % 360;
        if (normAngle < 0)
            normAngle = 360 + normAngle;

        int spriteIndex = 0;
        if (AsteroidsState.time - lastThrustTime < THRUST_INTERVAL)
            spriteIndex = AsteroidsState.time - lastThrustTime + 1;

        builder.addItem(spriteId,
                (int) position.getX(), (int) position.getY(),
                Math.toRadians(normAngle), size / SPRITE_SIZE,
                spriteIndex * SPRITE_SIZE, 0,
                SPRITE_SIZE, SPRITE_SIZE);

        if (isShieldActive())
            drawSprite(builder, 0, 0,
                    (int) (SHIELD_RADIUS * 2), Math.toRadians(heading));

    }

    @Override
    public boolean expired() {
        return !isAlive();
    }

    /**
     * Load all sprites to the {@link GameMovieBuilder} builder
     *
     * @param builder {@link GameMovieBuilder} builder
     */
    public static void loadSprites(GameMovieBuilder builder) {

        // load ship sprites
        for (int i = 0; i < SPRITES.length; i++) {
            builder.addSprite(String.format(SPRITE_ID_FORMAT, i), SPRITES[i]);
        }

        // load shield sprites
        for (int i = 0; i < SHIELD_SPRITES.length; i++) {
            builder.addSprite(String.format(SHIELD_SPRITE_ID_FORMAT, i), SHIELD_SPRITES[i]);
        }
    }
}
