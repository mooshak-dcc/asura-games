package pt.up.fc.dcc.asura;

import pt.up.fc.dcc.asura.builder.base.exceptions.PlayerException;
import pt.up.fc.dcc.asura.builder.base.messaging.Command;
import pt.up.fc.dcc.asura.builder.base.messaging.PlayerAction;
import pt.up.fc.dcc.asura.builder.base.messaging.StateUpdate;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.builder.base.GameState;
import pt.up.fc.dcc.asura.builder.base.movie.models.MooshakClassification;
import pt.up.fc.dcc.asura.messaging.ShipCommand;
import pt.up.fc.dcc.asura.messaging.ShipUpdate;
import pt.up.fc.dcc.asura.models.*;
import pt.up.fc.dcc.asura.models.effects.*;
import pt.up.fc.dcc.asura.utils.Vector;

import java.util.*;
import java.util.logging.Logger;

/**
 * Stores the current state of the Asteroids game
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class AsteroidsState implements GameState {
    public static final int GAME_WORLD_WIDTH = 600;
    public static final int GAME_WORLD_HEIGHT = 600;
    private static final String MSG_WINNER = "Congratulations, %s! Your accuracy surprises me.";
    private static final String MSG_LOSER = "%s, what can I say to you?! You were blown away... by a fake asteroid.";
    private static final String MSG_LOSER_ALIVE = "%s, you have not accomplished your task.";
    private static final String BACKGROUND_SPRITE_ID = "background";
    private static final String BACKGROUND_SPRITE = "background.jpg";
    private static final int BACKGROUND_WIDTH = 2400;
    private static final int BACKGROUND_HEIGHT = 640;
    private static final int FPS = 60;
    private static final int MAX_NUMBER_INITIAL_ASTEROIDS = 4;
    private static final int MIN_ASTEROID_SHIP_DISTANCE = 125;
    private static final int[][] INITIAL_POSITIONS = {
            new int[] { GAME_WORLD_WIDTH / 4, GAME_WORLD_HEIGHT / 4},
            new int[] { GAME_WORLD_WIDTH * 3 / 4, GAME_WORLD_HEIGHT * 3 / 4},
            new int[] { GAME_WORLD_WIDTH * 3 / 4, GAME_WORLD_HEIGHT / 4},
            new int[] { GAME_WORLD_WIDTH / 4, GAME_WORLD_HEIGHT * 3 / 4},
    };

    private static final int HIT_ASTEROID_SCORE = 2;
    private static final int DESTROYED_ASTEROID_SCORE = 4;
    private static final int ASTEROID_PROTECTION_SCORE = 2;
    private static final int HEALTH_5_POINT_SCORE = 2;

    // global game time i.e. current frame
    private int time = 1;

    private int backgroundX = 0;

    private Map<String, String> players;

    private List<Asteroid> asteroids = new ArrayList<>();
    private Map<String, Ship> ships = new HashMap<>();
    private Map<String, List<Bullet>> bullets = new HashMap<>();
    private List<EffectActor> effects = new ArrayList<>();

    private Map<String, ShipCommand> roundCommands = new HashMap<>();

    private LinkedList<Ship> expiredShips = new LinkedList<>();
    private Map<String, LinkedList<Bullet>> bulletResults = new HashMap<>();
    private Map<Bullet, Asteroid> targets = new HashMap<>();

    @Override
    public void prepare(GameMovieBuilder movieBuilder, String title, Map<String, String> players) {

        this.players = players;

        movieBuilder.setTitle(title);
        movieBuilder.setWidth(GAME_WORLD_WIDTH);
        movieBuilder.setHeight(GAME_WORLD_HEIGHT);
        movieBuilder.setFps(FPS);
        movieBuilder.setSpriteAnchor(GameMovieBuilder.SpriteAnchor.CENTER);

        // load all sprites
        movieBuilder.addSprite(BACKGROUND_SPRITE_ID, BACKGROUND_SPRITE);
        Asteroid.loadSprites(movieBuilder);
        Ship.loadSprites(movieBuilder);
        Bullet.loadSprites(movieBuilder);
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
                    position, (((int) Math.toDegrees(position.headingTo(worldCenter)) - 90) + 360) % 360));
            bullets.put(playerId, new LinkedList<>());
            bulletResults.put(playerId, new LinkedList<>());
        }

        // load asteroids
        for (int i = 0; i < MAX_NUMBER_INITIAL_ASTEROIDS; i++)
            asteroids.add(generateAsteroid());

        buildFrame(movieBuilder);
    }

    @Override
    public void execute(GameMovieBuilder movieBuilder, String playerId, PlayerAction action) {

        Command command = action.getCommand();

        if (command != null) {
            Object[] args = command.getArgs();

            try {
                switch (command.getName()) {
                    case "SHIP":
                        if (args.length != 5)
                            throw new IllegalArgumentException("SHIP: five arguments expected" +
                                    " (thrust, steerLeft, steerRight, shield, fire)");

                        boolean thrust = Boolean.parseBoolean(String.valueOf(args[0]));
                        boolean steerLeft = Boolean.parseBoolean(String.valueOf(args[1]));
                        boolean steerRight = Boolean.parseBoolean(String.valueOf(args[2]));
                        boolean shield = Boolean.parseBoolean(String.valueOf(args[3]));

                        int fire;
                        try {
                            fire = (int) Double.parseDouble(String.valueOf(args[4]));
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("SHIP: fire expects an integer, " +
                                    "got '" + String.valueOf(args[4]) + "'");
                        }

                        roundCommands.put(playerId, new ShipCommand(thrust,
                                steerLeft, steerRight, shield, fire, action.getMessages()));

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

        // update ship state
        update.setX((int) position.getX());
        update.setY((int) position.getY());
        update.setVelocity(velocity.length());
        update.setHeading(ship.getHeading());
        update.setHealth(ship.getHealth());
        update.setEnergy(ship.getEnergy());
        update.setShieldCounter(ship.getShieldActiveCounter());

        // asteroids
        asteroids.sort((o1, o2) -> {
            double dist1 = ship.getPosition().distance(o1.getPosition());
            double dist2 = ship.getPosition().distance(o2.getPosition());
            return Double.compare(dist1, dist2);
        });
        for (Asteroid asteroid: asteroids) {

            update.addAsteroid(
                    (int) asteroid.getPosition().getX(),
                    (int) asteroid.getPosition().getY());
        }

        return new StateUpdate("FULL_UPDATE", update);
    }

    @Override
    public void endRound(GameMovieBuilder movieBuilder) {

        // execute actions
        for (String playerId: roundCommands.keySet()) {

            ShipCommand shipCommand = roundCommands.get(playerId);

            Ship ship = findPlayerShip(playerId);
            if (ship == null || ship.expired(time))
                continue;

            if (shipCommand.isSteerLeft())
                ship.steerLeft();

            if (shipCommand.isSteerRight())
                ship.steerRight();

            if (shipCommand.isThrust())
                ship.thrust(time);

            if (shipCommand.isShield())
                ship.activateShield();

            switch (shipCommand.getFire()) {
                case 1:
                    int pos = bullets.get(playerId).size();
                    ship.firePrimary(time, bullets.get(playerId));
                    targets.put(bullets.get(playerId).get(pos), asteroids.get(0));
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

        // reset state
        roundCommands.clear();

        time++;
    }

    @Override
    public boolean isRunning() {
        return ships.size() >= 1 && time < 10000 && asteroids.size() > 0 ||
                !expiredShips.isEmpty() && expiredShips.getLast().getKilledOn() > (time - FPS * 2);
    }

    @Override
    public void finalize(GameMovieBuilder movieBuilder) {

        movieBuilder.saveFrame();

        movieBuilder.addFrame();

        movieBuilder.restoreFrame();

        for (String playerId: players.keySet())
            updatePlayerStatus(movieBuilder, playerId);
    }

    /**
     * Find a player ship by its ID.
     *
     * @param playerId {@link String} ID of the player
     * @return {@link Ship} ship of the player
     */
    private Ship findPlayerShip(String playerId) {
        Ship ship = ships.get(playerId);
        if (ship == null) {
            for (Ship expired: expiredShips) {
                if (expired.getPlayerId().equals(playerId)) {
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

        // add messages
        for (String playerId: roundCommands.keySet()) {
            ShipCommand shipCommand = roundCommands.get(playerId);

            List<String> messages = shipCommand.getMessages();
            if (messages != null) {
                StringBuilder msgBuilder = new StringBuilder();
                for (String message: messages) {
                    msgBuilder.append(message).append("\n");
                }
                movieBuilder.addMessage(playerId, msgBuilder.toString());
            }
        }

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
            asteroid.draw(time, movieBuilder);

        for (List<Bullet> playerBullets: bullets.values())
            for (Bullet bullet: playerBullets)
                bullet.draw(time, movieBuilder);

        for (Ship ship: ships.values())
            ship.draw(time, movieBuilder);

        for (EffectActor effect: effects)
            effect.draw(time, movieBuilder);
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

            if (actor.expired(time)) {

                if (actor instanceof Ship) {
                    expiredShips.add(ships.remove(((Ship) actor).getPlayerId()));
                } else if (actor instanceof Bullet) {
                    Bullet bullet = ((Bullet) actor);
                    if (bullet.getResult() == null)
                        bullet.setResult(BulletResult.NO_HIT);

                    bulletResults.get(bullet.getPlayerId()).addLast(bullet);
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
            Ship owner = findPlayerShip(playerId);
            if (owner == null)
                continue;

            List<Bullet> playerBullets = bullets.get(playerId);
            for (Bullet bullet: playerBullets) {

                for (int i = 0, size = asteroids.size(); i < size; i++) {
                    Asteroid asteroid = asteroids.get(i);

                    if (bullet.collides(asteroid)) {
                        bullet.hit(-1);

                        // add animation
                        effects.add(bullet.getHitEffect(time, asteroid));

                        if (asteroid.hit(bullet.power())) {

                            if (!targets.get(bullet).equals(asteroid)) {
                                bullet.setResult(BulletResult.NO_HIT);
                                Logger.getLogger("").severe("No hit.");
                            } else {
                                bullet.setResult(BulletResult.DESTROYED_ASTEROID);

                                owner.addScorePoints(DESTROYED_ASTEROID_SCORE);
                            }
                            // add animation
                            effects.add(asteroid.getExplosionEffect(time, bullet));
                        } else {
                            if (!targets.get(bullet).equals(asteroid)) {
                                bullet.setResult(BulletResult.NO_HIT);
                                Logger.getLogger("").severe("No hit.");
                            } else {
                                bullet.setResult(BulletResult.HIT_ASTEROID);

                                owner.addScorePoints(HIT_ASTEROID_SCORE);
                            }

                            // add animation
                            effects.add(asteroid.getHitEffect(time, bullet));
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

                    effects.add(asteroid.getHitEffect(time, ship));

                    if (!ship.isShieldActive()) {

                        ship.hit(time, -1);
                        effects.add(ship.getExplosionEffect(time, asteroid));
                    } else {
                        asteroid.hit(-1);

                        ship.addScorePoints(ASTEROID_PROTECTION_SCORE);

                        ship.setVelocity(new Vector(0, 0));
                        effects.add(asteroid.getExplosionEffect(time, ship));
                        return;
                    }

                    if (asteroid.hit(2)) {
                        effects.add(asteroid.getExplosionEffect(time, ship));
                    }
                }
            }
        }
    }

    /**
     * Generate and place asteroids on the game world at a minimum distance from the players.
     *
     * @return {@link Asteroid} the generated asteroid
     */
    private Asteroid generateAsteroid() {

        while (true) {

            // perform a test to check it is not too close to the player
            Vector position = new Vector(Math.random() * GAME_WORLD_WIDTH, Math.random() * GAME_WORLD_HEIGHT);

            boolean placed = true;
            for (Ship ship: ships.values()) {

                if (ship.expired(time))
                    continue;

                if (ship.getPosition().distance(position) < MIN_ASTEROID_SHIP_DISTANCE) {
                    placed = false;
                    break;
                }
            }

            if (placed)
                return Asteroid.createAsteroid(position, new Vector(0, 0));
        }
    }

    /**
     * Update status of a player with classification, points, observations and final messages.
     *
     * @param movieBuilder {@link GameMovieBuilder} the game movie builder
     * @param playerId {@link String} ID of the player
     */
    private void updatePlayerStatus(GameMovieBuilder movieBuilder, String playerId) {

        String name = players.get(playerId);
        Ship ship = findPlayerShip(playerId);

        if (ship == null)
            return;

        // set observations based on behaviour
        if (ship.isAlive()) {
            //ship.addScorePoints((int) (Math.floor(ship.getHealth() / 5) * HEALTH_5_POINT_SCORE));

            long destroyed = bulletResults.get(playerId).stream()
                    .filter(b -> b.getResult() != null && b.getResult().equals(BulletResult.DESTROYED_ASTEROID))
                    .count();
            if (destroyed < 3) {
                movieBuilder.addMessage(playerId, String.format(MSG_LOSER_ALIVE, name));
                throw new PlayerException(playerId, MooshakClassification.WRONG_ANSWER, "You must destroy at " +
                        "least 3 asteroids with bullets. You have destroyed " + destroyed + ".");
            }

            long hits = bulletResults.get(playerId).stream()
                    .filter(b -> b.getResult() != null && !b.getResult().equals(BulletResult.NO_HIT))
                    .count();

            double ratio = (double) hits / bulletResults.get(playerId).size();

            if (ratio < 0.8) {
                movieBuilder.addMessage(playerId, String.format(MSG_LOSER_ALIVE, name));
                throw new PlayerException(playerId, MooshakClassification.WRONG_ANSWER, "You must have a hit " +
                        "ratio of at least 80%. You had " + String.format("%.2f", ratio * 100) + "%.");
            }

            movieBuilder.addMessage(playerId, String.format(MSG_WINNER, playerId));
        } else {
            movieBuilder.addMessage(playerId, String.format(MSG_LOSER, name));
            throw new PlayerException(playerId, MooshakClassification.WRONG_ANSWER, String.format(MSG_LOSER, name));
        }

        // set player submission status
        movieBuilder.setClassification(playerId, MooshakClassification.ACCEPTED);
        movieBuilder.setPoints(playerId, ship.getScore());

        // set the observations
        movieBuilder.setObservations(playerId, "Congratulations!");
    }
}
