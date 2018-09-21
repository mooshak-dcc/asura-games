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

/**
 * Stores the current state of the Asteroids game
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class AsteroidsState implements GameState {
    public static final int GAME_WORLD_WIDTH = 600;
    public static final int GAME_WORLD_HEIGHT = 600;
    private static final String MSG_WINNER = "Congratulations, %s! You have learned how to use promises in war.";
    private static final String MSG_LOSER = "%s, try again. I can't believe you are not capable.";
    private static final String BACKGROUND_SPRITE_ID = "background";
    private static final String BACKGROUND_SPRITE = "background.jpg";
    private static final int BACKGROUND_WIDTH = 2400;
    private static final int BACKGROUND_HEIGHT = 640;
    private static final int FPS = 60;
    private static final int NUMBER_SHIP_ENEMIES = 3;
    private static final int MIN_ENEMIES_SHIP_DISTANCE = 295;
    private static final double RANGE_SHIP_WARN = 250;
    private static final double RANGE_BULLET_WARN = 50;

    private static final int HIT_SHIP_SCORE = 5;
    private static final int DESTROYED_SHIP_SCORE = 10;
    private static final int BULLET_PROTECTION_SCORE = 4;
    private static final int SHOT_ON_PROTECTION_SCORE = 2;

    // global game time i.e. current frame
    private long time = 1;

    private int backgroundX = 0;

    private Map<String, String> players;

    private List<Asteroid> asteroids = new ArrayList<>();
    private Map<String, Ship> ships = new HashMap<>();
    private Map<String, List<Bullet>> bullets = new HashMap<>();
    private List<EffectActor> effects = new ArrayList<>();

    private Map<String, ShipCommand> roundCommands = new HashMap<>();

    private LinkedList<Ship> expiredShips = new LinkedList<>();
    private Map<String, LinkedList<Bullet>> bulletResults = new HashMap<>();

    private List<Ship> enemies = new ArrayList<>();

    private Ship currentTarget = null;
    private int failed = 0;
    private int hits = 0;

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
            Vector position = new Vector(GAME_WORLD_WIDTH / 2, GAME_WORLD_HEIGHT / 2);
            ships.put(playerId, Ship.create(playerId, position, 0));
            bullets.put(playerId, new LinkedList<>());
            bulletResults.put(playerId, new LinkedList<>());
        }

        // load some ships
        for (int i = 0; i < NUMBER_SHIP_ENEMIES; i++)
            enemies.add(generateShip());

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

        // add bullet results
        LinkedList<Bullet> oldBullets = bulletResults.get(player);
        while (!oldBullets.isEmpty()) {
            Bullet oldBullet = oldBullets.removeFirst();
            update.addBulletResult(oldBullet.getBulletId(), oldBullet.getResult());
        }

        // add actors

        // bullets within a range of 50 pixels
        for (String playerId: bullets.keySet()) {

            if (playerId.equals(player))
                continue;

            List<Bullet> playerBullets = bullets.get(playerId);
            for (Bullet bullet: playerBullets) {

                if (ship.withinRange(bullet, RANGE_BULLET_WARN)) {
                    update.addActor(ShipUpdate.Type.BULLET,
                            (int) bullet.getPosition().getX(),
                            (int) bullet.getPosition().getY());
                }
            }
        }

        // ships within a range of 250 pixels
        for (String playerId: ships.keySet()) {

            if (playerId.equals(player))
                continue;

            Ship otherShip = ships.get(playerId);

            if (ship.withinRange(otherShip, RANGE_SHIP_WARN)) {
                update.addActor(ShipUpdate.Type.SHIP,
                        (int) otherShip.getPosition().getX(),
                        (int) otherShip.getPosition().getY());
            }
        }

        // ships within a range of 250 pixels
        for (Ship enemy: enemies) {

            if (ship.withinRange(enemy, RANGE_SHIP_WARN)) {
                update.addActor(ShipUpdate.Type.SHIP,
                        (int) enemy.getPosition().getX(),
                        (int) enemy.getPosition().getY());
            }
        }

        // asteroids
        for (Asteroid asteroid: asteroids) {

            update.addActor(ShipUpdate.Type.ASTEROID,
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

            /*if (shipCommand.isThrust())
                ship.thrust();*/

            if (shipCommand.isShield())
                ship.activateShield();

            switch (shipCommand.getFire()) {
                case 1:
                    ship.firePrimary(time, bullets.get(playerId));
                    break;
            }
        }

        // update all actors
        updateActors();

        // draw
        buildFrame(movieBuilder);

        // do collision checks
        processBulletCollisions();
//        processAsteroidsCollisions();

        // add asteroids to world if it is too empty
//        if (time % 1000 == 0 && asteroids.size() < MAX_NUMBER_ASTEROIDS)
//            asteroids.add(generateAsteroid());

        // reset state
        roundCommands.clear();

        time++;
    }

    @Override
    public boolean isRunning() {
        return ships.size() >= 1 && time < 10000 && !enemies.isEmpty();
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

        for (Ship ship: enemies)
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

        updateActors(enemies);

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
                    if (bullet.getResult() == null) {
                        bullet.setResult(BulletResult.NO_HIT);
                        if (currentTarget != null) {
                            failed++;
                            currentTarget = null;
                        }
                    }

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

                List<Ship> allShips = new ArrayList<>(ships.values());
                allShips.addAll(enemies);
                for (Ship ship: allShips) {

                    // check for other ships
                    if (bullet.collides(ship)) {
                        bullet.hit(-1);

                        // add animation
                        effects.add(bullet.getHitEffect(time, ship));

                        // bullets don't kill self
                        if (ship.getPlayerId().equals(playerId))
                            continue;

                        // shield doesn't let you die
                        if (ship.isShieldActive()) {
                            bullet.setResult(BulletResult.HIT_SHIELD);

                            if (currentTarget == null) {
                                currentTarget = ship;
                                hits++;
                            } else if (!currentTarget.getPlayerId().equals(ship.getPlayerId())) {
                                failed++;
                                currentTarget = ship;
                            } else
                                hits++;

                            ship.addScorePoints(BULLET_PROTECTION_SCORE);
                            owner.addScorePoints(SHOT_ON_PROTECTION_SCORE);

                            continue;
                        }

                        if (ship.hit(time, bullet.power())) {
                            bullet.setResult(BulletResult.DESTROYED_SHIP);

                            if (currentTarget != null && !currentTarget.getPlayerId().equals(ship.getPlayerId()))
                                failed++;
                            else if (currentTarget != null)
                                hits++;

                            currentTarget = null;

                            owner.addScorePoints(DESTROYED_SHIP_SCORE);

                            // add animation
                            effects.add(ship.getExplosionEffect(time, bullet));
                        } else {
                            bullet.setResult(BulletResult.HIT_SHIP);

                            if (currentTarget == null) {
                                currentTarget = ship;
                                hits++;
                            } else if (!currentTarget.getPlayerId().equals(ship.getPlayerId())) {
                                failed++;
                                currentTarget = ship;
                            } else
                                hits++;

                            owner.addScorePoints(HIT_SHIP_SCORE);

                            // add animation
                            effects.add(ship.getHitEffect(time, bullet));
                        }
                    }
                }
            }
        }
    }

    /**
     * Generate and place ships on the game world at a minimum distance from the players.
     *
     * @return {@link Ship} the generated ship
     */
    private Ship generateShip() {

        while (true) {

            // perform a test to check it is not too close to the player
            Vector position = new Vector(Math.random() * GAME_WORLD_WIDTH, Math.random() * GAME_WORLD_HEIGHT);

            boolean placed = true;
            for (Ship ship: ships.values()) {

                if (ship.expired(time))
                    continue;

                if (ship.getPosition().distance(position) < MIN_ENEMIES_SHIP_DISTANCE) {
                    placed = false;
                    break;
                }
            }

            for (Ship ship: enemies) {

                if (ship.expired(time))
                    continue;

                if (ship.getPosition().distance(position) < MIN_ENEMIES_SHIP_DISTANCE/4) {
                    placed = false;
                    break;
                }
            }

            if (placed)
                return Ship.create("enemy" + enemies.size(), position, (int) (Math.random() * 360));
        }
    }

    private static final int HEALTH_TOLERANCE = 10;
    private static final int ENERGY_TOLERANCE = 20;
    private static final double RATIO_FIRE_COUNT_SCORE = 0.5D;

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

        if (enemies.size() >= 3) {
            movieBuilder.addMessage(playerId, String.format(MSG_LOSER, name));
            throw new PlayerException(playerId, MooshakClassification.WRONG_ANSWER, "You need to destroy at least " +
                    "one ship!");
        }

        double ratio = (double) hits / (hits + failed);
        if (ratio < 0.9) {
            movieBuilder.addMessage(playerId, String.format(MSG_LOSER, name));
            throw new PlayerException(playerId, MooshakClassification.WRONG_ANSWER, "You need at least 90% of " +
                    "hit ratio! You have got " + String.format("%.2f", ratio * 100) + "%.");
        }

        movieBuilder.addMessage(playerId, String.format(MSG_WINNER, name));

        // set player submission status
        movieBuilder.setClassification(playerId, MooshakClassification.ACCEPTED);
        movieBuilder.setPoints(playerId, ship.getScore());

        // set the observations
        movieBuilder.setObservations(playerId, "Congratulations!");
    }
}
