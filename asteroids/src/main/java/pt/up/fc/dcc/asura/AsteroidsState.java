package pt.up.fc.dcc.asura;

import pt.up.fc.dcc.asura.builder.base.messaging.Command;
import pt.up.fc.dcc.asura.builder.base.messaging.PlayerAction;
import pt.up.fc.dcc.asura.builder.base.messaging.StateUpdate;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.builder.base.GameState;
import pt.up.fc.dcc.asura.messaging.ShipCommand;
import pt.up.fc.dcc.asura.messaging.ShipUpdate;
import pt.up.fc.dcc.asura.models.*;
import pt.up.fc.dcc.asura.utils.Vector;

import java.util.*;

/**
 * Stores the current state of the Asteroids game
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class AsteroidsState implements GameState {
    public static final int GAME_WORLD_WIDTH = 600;
    public static final int GAME_WORLD_HEIGHT = 600;
    private static final String MSG_WINNER = "Congratulations, %s! You have survived during the bloody War of Asteroids." +
            " Your name, %s, will be remembered by the whole world (even if only you survived).";
    private static final String MSG_MULTIPLE_WINNER = "Congratulations, %s! There is still a lot of work to deworm the " +
            "space, but you will have more chances.";
    private static final String MSG_LOSER = "%s, what can I say to you?! You were blown away. Get your ship fixed before" +
            " competing with the space giants.";
    private static final String BACKGROUND_SPRITE_ID = "background";
    private static final String BACKGROUND_SPRITE = "background.jpg";
    private static final int BACKGROUND_WIDTH = 2400;
    private static final int BACKGROUND_HEIGHT = 640;
    private static final int FPS = 60;
    private static final int MAX_NUMBER_INITIAL_ASTEROIDS = 5;
    private static final int MAX_NUMBER_ASTEROIDS = 10;
    private static final int MIN_ASTEROID_SHIP_DISTANCE = 125;
    private static final int[][] INITIAL_POSITIONS = {
            new int[] { GAME_WORLD_WIDTH / 4, GAME_WORLD_HEIGHT / 4},
            new int[] { GAME_WORLD_WIDTH * 3 / 4, GAME_WORLD_HEIGHT * 3 / 4},
            new int[] { GAME_WORLD_WIDTH * 3 / 4, GAME_WORLD_HEIGHT / 4},
            new int[] { GAME_WORLD_WIDTH / 4, GAME_WORLD_HEIGHT * 3 / 4},
    };

    // global game time i.e. current frame
    public static int time = 1;

    private int backgroundX = 0;

    private Map<String, String> players;

    private List<Asteroid> asteroids = new ArrayList<>();
    private Map<String, Ship> ships = new HashMap<>();
    private Map<String, List<Bullet>> bullets = new HashMap<>();
    private List<EffectActor> effects = new ArrayList<>();

    private LinkedList<Ship> expiredShips = new LinkedList<>();

    private Map<String, ShipCommand> roundCommands = new HashMap<>();

    @Override
    public void prepare(GameMovieBuilder movieBuilder, String title, Map<String, String> players) {

        this.players = players;

        movieBuilder.setTitle(title);
        movieBuilder.setWidth(GAME_WORLD_WIDTH);
        movieBuilder.setHeight(GAME_WORLD_HEIGHT);
        //movieBuilder.setBackground("background.jpg");
        movieBuilder.setFps(FPS);
        movieBuilder.setSpriteAnchor(GameMovieBuilder.SpriteAnchor.CENTER);

        // load all sprites
        movieBuilder.addSprite(BACKGROUND_SPRITE_ID, BACKGROUND_SPRITE);
        Asteroid.loadSprites(movieBuilder);
        Ship.loadSprites(movieBuilder);
        Bullet.loadSprites(movieBuilder);
        Star.loadSprites(movieBuilder);
        BulletHitEffect.loadSprites(movieBuilder);
        AsteroidHitEffect.loadSprites(movieBuilder);
        AsteroidExplosionEffect.loadSprites(movieBuilder);
        ShipExplosionEffect.loadSprites(movieBuilder);
        ShipHitEffect.loadSprites(movieBuilder);

        Vector worldCenter = new Vector(GAME_WORLD_WIDTH / 2, GAME_WORLD_HEIGHT / 2);

        // load players
        for (String playerId : players.keySet()) {
            movieBuilder.addPlayer(playerId, players.get(playerId));
            Vector position = new Vector(
                    (int) (INITIAL_POSITIONS[ships.size()][0] * (Math.random() * 0.2 + 0.9)),
                    (int) (INITIAL_POSITIONS[ships.size()][1] * (Math.random() * 0.2 + 0.9)));
            ships.put(playerId, Ship.create(playerId,
                    position, (int) Math.toDegrees(position.headingTo(worldCenter)) - 90));
            bullets.put(playerId, new LinkedList<>());
        }

        // load asteroids
        int nrAsteroids = (int) (Math.random() * MAX_NUMBER_INITIAL_ASTEROIDS + 1);
        for (int i = 0; i < nrAsteroids; i++)
            asteroids.add(generateAsteroid());


        movieBuilder.addFrame();

        for (Ship ship: ships.values()) {
            ship.draw(movieBuilder);
        }
    }

    @Override
    public void execute(GameMovieBuilder movieBuilder, String playerId, PlayerAction action) {

        Command command = action.getCommand();

        if (command != null) {
            Object[] args = command.getArgs();

            try {
                switch (command.getName()) {
                    case "PLAY":
                        if (args.length != 5)
                            throw new IllegalArgumentException("PLAY: four arguments expected" +
                                    " (thrust, steerLeft, steerRight, shield, fire)");

                        boolean thrust = Boolean.parseBoolean(String.valueOf(args[0]));
                        boolean steerLeft = Boolean.parseBoolean(String.valueOf(args[1]));
                        boolean steerRight = Boolean.parseBoolean(String.valueOf(args[2]));

                        boolean shield = Boolean.parseBoolean(String.valueOf(args[3]));

                        int fire;
                        try {
                            fire = (int) Double.parseDouble(String.valueOf(args[4]));
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("PLAY: fire expects an integer, " +
                                    "got '" + String.valueOf(args[4]) + "'");
                        }

                        /*Logger.getLogger("").severe(thrust + " " + steerLeft + " " +
                                steerRight + " " + shield + " " + fire);*/

                        roundCommands.put(playerId, new ShipCommand(thrust,
                                steerLeft, steerRight, shield, fire));

                        break;
                    default:
                        throw new IllegalArgumentException(command.getName());
                }
            } catch (IllegalArgumentException e) {
                movieBuilder.addFrame();
                movieBuilder.wrongAnswer(playerId, e.getMessage());
                throw e;
            }
        }


    }

    @Override
    public StateUpdate getStateUpdateFor(String player) {

        ShipUpdate update = new ShipUpdate();

        Ship ship = findPlayerShip(player);
        if (ship == null) return null;

        Vector position = ship.getPosition();
        Vector velocity = ship.getVelocity();

        update.setX((int) position.getX());
        update.setY((int) position.getY());
        update.setVelocity(velocity.length());
        update.setHeading(ship.getHeading());
        update.setHealth(ship.getHealth());
        update.setEnergy(ship.getEnergy());
        update.setShieldCounter(ship.getShieldActiveCounter());

        return new StateUpdate("SHIP", update);
    }

    @Override
    public void endRound(GameMovieBuilder movieBuilder) {

        // TODO

        // execute actions
        for (String playerId: roundCommands.keySet()) {

            ShipCommand shipCommand = roundCommands.get(playerId);

            Ship ship = findPlayerShip(playerId);
            if (ship == null || ship.expired())
                continue;

            if (shipCommand.isSteerLeft())
                ship.steerLeft();

            if (shipCommand.isSteerRight())
                ship.steerRight();

            if (shipCommand.isThrust())
                ship.thrust();

            if (shipCommand.isShield())
                ship.activateShield();

            switch (shipCommand.getFire()) {
                case 1:
                    ship.firePrimary(bullets.get(playerId));
                    break;
                case 2:

                    break;
            }
        }

        // update all actors
        updateActors();

        // draw
        buildFrame(movieBuilder);

        // do collision checks
        processBulletCollisions();
        processAsteroidsCollisions();

        // add asteroids to world if it is too empty
        if (time % 1000 == 0 && asteroids.size() < MAX_NUMBER_ASTEROIDS)
            asteroids.add(generateAsteroid());

        time++;
    }

    @Override
    public boolean isRunning() {
        return ships.size() > 1 && time < 10000 ||
                expiredShips.getLast().getKilledOn() > (time - FPS * 2);
    }

    @Override
    public void finalize(GameMovieBuilder movieBuilder) {

        // TODO

        movieBuilder.addFrame();

        for (String playerId: ships.keySet()) {

            String name = players.get(playerId);

            if (ships.size() > 1)
                movieBuilder.addMessage(playerId, String.format(MSG_MULTIPLE_WINNER, name));
            else
                movieBuilder.addMessage(playerId, String.format(MSG_WINNER, name, name));
        }

        for (Ship expired: expiredShips) {
            String name = players.get(expired.getPlayerId());
            movieBuilder.addMessage(expired.getPlayerId(), String.format(MSG_LOSER, name));
        }
    }

    private Ship findPlayerShip(String player) {
        Ship ship = ships.get(player);
        if (ship == null) {
            for (Ship expired: expiredShips) {
                if (expired.getPlayerId().equals(player)) {
                    ship = expired;
                    break;
                }
            }

            if (ship == null)
                return null;
        }
        return ship;
    }

    /**
     * Build a frame of the game
     *
     * @param movieBuilder {@link GameMovieBuilder} the game movie builder
     */
    private void buildFrame(GameMovieBuilder movieBuilder) {

        // add frame
        movieBuilder.addFrame();

        // REMEMBER: anchor is the center (we can ignore x, but not y)
        movieBuilder.addItem(BACKGROUND_SPRITE_ID, backgroundX ,
                BACKGROUND_HEIGHT/2);
        if (++backgroundX >= BACKGROUND_WIDTH / 2)
            backgroundX -= BACKGROUND_WIDTH / 2;

        // draw the actors
        drawActors(movieBuilder);
    }

    /**
     * Draw all actors on the frame.
     *
     * @param movieBuilder {@link GameMovieBuilder} the game movie builder
     */
    private void drawActors(GameMovieBuilder movieBuilder) {

        for (Asteroid asteroid: asteroids) 
            asteroid.draw(movieBuilder);

        for (List<Bullet> playerBullets: bullets.values())
            for (Bullet bullet: playerBullets)
                bullet.draw(movieBuilder);

        for (Ship ship: ships.values())
            ship.draw(movieBuilder);

        for (EffectActor effect: effects)
            effect.draw(movieBuilder);
    }

    /**
     * Update all actors based on expiration and velocity vector.
     */
    private void updateActors() {

        updateActors(asteroids);

        for (List<Bullet> playerBullets: bullets.values())
            updateActors(playerBullets);

        updateActors(new ArrayList<>(ships.values()));

        updateActors(effects);
    }

    /**
     * Update an actor subset based on expiration and velocity vector.
     *
     * @param actors {@link List<Actor>} list of actors to update
     */
    private void updateActors(List<? extends Actor> actors) {

        int removedCount = 0;
        for (int i = 0, size = actors.size(); i < size; i++) {

            Actor actor = actors.get(i - removedCount);

            actor.onUpdate();

            if (actor.expired()) {

                if (actor instanceof Ship) {
                    expiredShips.add(ships.remove(((Ship) actor).getPlayerId()));
                }

                actors.remove(i - removedCount);
                removedCount++;
            } else {
                updateActorPosition(actor);
            }
        }
    }

    /**
     * Update actor position using its current actor position.
     *
     * @param actor {@link Actor} actor to update position
     */
    private void updateActorPosition(Actor actor) {

        Vector position = actor.getPosition();

        // update actor using its current vector
        actor.getPosition().add(actor.getVelocity());

        // handle traversing out of the coordinate space and back again
        if (position.getX() >= GAME_WORLD_WIDTH)
            position.setX(0);
        else if (position.getX() < 0)
            position.setX(GAME_WORLD_WIDTH - 1);
        if (position.getY() >= GAME_WORLD_HEIGHT)
            position.setY(0);
        else if (position.getY() < 0)
            position.setY(GAME_WORLD_HEIGHT - 1);

    }

    /**
     * Process the existing collisions with bullets in the game world
     */
    private void processBulletCollisions() {

        for (String playerId: bullets.keySet()) {
            Ship owner = ships.get(playerId);
            List<Bullet> playerBullets = bullets.get(playerId);
            for (Bullet bullet: playerBullets) {

                for (int i = 0, size = asteroids.size(); i < size; i++) {
                    Asteroid asteroid = asteroids.get(i);
                    if (bullet.collides(asteroid)) {
                        bullet.hit(-1);

                        // add animation
                        effects.add(bullet.getHitEffect(asteroid));

                        if (asteroid.hit(bullet.power())) {
                            generateBabyAsteroids(asteroid, bullet);

                            // add animation
                            effects.add(asteroid.getExplosionEffect(bullet));
                        } else {
                            applyBulletImpactOnAsteroid(bullet, asteroid);

                            // add animation
                            effects.add(asteroid.getHitEffect(bullet));
                        }
                    }
                }

                for (Ship ship: ships.values()) {

                    // check for other ships
                    if (bullet.collides(ship)) {
                        bullet.hit(-1);

                        // add animation
                        effects.add(bullet.getHitEffect(ship));

                        // bullets don't kill self
                        if (ship.getPlayerId().equals(playerId))
                            continue;

                        // shield doesn't let you die
                        if (ship.isShieldActive())
                            continue;

                        if (ship.hit(bullet.power())) {

                            // add animation
                            effects.add(ship.getExplosionEffect(bullet));
                        } else {

                            // add animation
                            effects.add(ship.getHitEffect(bullet));
                        }
                    }
                }
            }
        }
    }

    /**
     * Process the existing collisions with asteroids in the game world.
     * Collisions with bullets already tested.
     */
    private void processAsteroidsCollisions() {

        for (int i = 0, size = asteroids.size(); i < size; i++) {

            Asteroid asteroid = asteroids.get(i);

            for (Ship ship: ships.values()) {

                if (asteroid.collides(ship)) {

                    effects.add(asteroid.getHitEffect(ship));

                    if (!ship.isShieldActive()) {

                        applyShipImpactOnAsteroid(ship, asteroid);

                        ship.hit(-1);
                        effects.add(ship.getExplosionEffect(asteroid));
                    } else {
                        asteroid.hit(-1);
                        generateBabyAsteroids(asteroid, ship);
                        ship.setVelocity(new Vector(0, 0));
                        effects.add(asteroid.getExplosionEffect(ship));
                        return;
                    }

                    if (asteroid.hit(2)) {
                        generateBabyAsteroids(asteroid, ship);
                        effects.add(asteroid.getExplosionEffect(ship));
                    }
                }
            }
        }

    }

    /**
     * Generate a number of baby asteroids from a destroyed asteroid. The number
     * and size of the generated asteroids are based on the parent size. Some of the
     * momentum of the parent vector (e.g. impacting bullet) is applied to the new asteroids.
     *
     * @param asteroid {@link Asteroid} the parent asteroid that has been destroyed
     * @param hitting {@link Actor} actor that destroyed the asteroid
     */
    private void generateBabyAsteroids(Asteroid asteroid, Actor hitting) {

        // generate some baby asteroid(s) if bigger than the minimum size
        if (asteroid.size() > 1) {

            Random random = new Random();

            int minBabies = Math.round(asteroid.size() / 2);
            int maxBabies = asteroid.size() - 1;
            int babies = maxBabies == minBabies ? 2 :
                    random.nextInt(maxBabies - minBabies) + minBabies;
            for (int i = 0; i < babies; i++) {

                int babySize = random.nextInt(asteroid.size() - 1) + 1;

                Vector velocity = (Vector) asteroid.getVelocity().clone();

                // apply a small random vector in the direction of travel
                Vector t = new Vector(0.0, -Math.random());

                // rotate by asteroid heading (adding noise)
                t.rotate2d(asteroid.getVelocity().angle2d() * (Math.random() * Math.PI)
                    + hitting.getVelocity().angle2d() * (Math.random() * Math.PI / 4));
                velocity.add(t);

                Vector hittingVelocity = (Vector) hitting.getVelocity().clone();
                hittingVelocity.scale(0.2);
                velocity.add(hittingVelocity);

                Asteroid baby = Asteroid.createAsteroid(
                        new Vector(asteroid.getPosition().getX() + Math.random() * (asteroid.radius() + 1),
                                asteroid.getPosition().getY() + Math.random() * (asteroid.radius() + 1)),
                        velocity, babySize, asteroid.type());

                asteroids.add(baby);
            }
        }
    }

    /**
     * Apply small bullet impact on asteroid
     *
     * @param bullet {@link Bullet} hitting bullet
     * @param asteroid {@link Asteroid} asteroid
     */
    private void applyBulletImpactOnAsteroid(Bullet bullet, Asteroid asteroid) {
        Vector bulletVelocity = (Vector) bullet.getVelocity().clone();

        bulletVelocity.scale((double) bullet.power() / (asteroid.size() * 4));

        asteroid.getVelocity().scale(0.95);
        asteroid.getVelocity().add(bulletVelocity);
    }

    /**
     * Apply small ship impact on asteroid
     *
     * @param ship {@link Ship} hitting ship
     * @param asteroid {@link Asteroid} asteroid
     */
    private void applyShipImpactOnAsteroid(Ship ship, Asteroid asteroid) {
        Vector shipVelocity = (Vector) ship.getVelocity().clone();

        shipVelocity.scale((double) 2 / (asteroid.size() * 4));

        asteroid.getVelocity().scale(0.9);
        asteroid.getVelocity().add(shipVelocity);
    }

    private Asteroid generateAsteroid() {
        while (true) {

            // perform a test to check it is not too close to the player
            Vector position = new Vector(Math.random() * GAME_WORLD_WIDTH, Math.random() * GAME_WORLD_HEIGHT);

            boolean placed = true;
            for (Ship ship: ships.values()) {

                if (ship.expired())
                    continue;

                if (ship.getPosition().distance(position) < MIN_ASTEROID_SHIP_DISTANCE) {
                    placed = false;
                    break;
                }
            }

            if (placed)
                return Asteroid.createAsteroid(position);
        }
    }
}
