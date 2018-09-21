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
    private static final String MSG_WINNER = "Congratulations, %s! You have survived the killer asteroids.";
    private static final String MSG_LOSER = "%s, that asteroid was on fire.";
    private static final String BACKGROUND_SPRITE_ID = "background";
    private static final String BACKGROUND_SPRITE = "background.jpg";
    private static final int BACKGROUND_WIDTH = 2400;
    private static final int BACKGROUND_HEIGHT = 640;
    private static final int FPS = 60;
    private static final int MAX_NUMBER_INITIAL_ASTEROIDS = 2;
    private static final int MAX_NUMBER_ASTEROIDS = 4;
    private static final int MIN_ASTEROID_SHIP_DISTANCE = 125;
    private static final int[][] INITIAL_POSITIONS = {
            new int[] { GAME_WORLD_WIDTH / 4, GAME_WORLD_HEIGHT / 4},
            new int[] { GAME_WORLD_WIDTH * 3 / 4, GAME_WORLD_HEIGHT * 3 / 4},
            new int[] { GAME_WORLD_WIDTH * 3 / 4, GAME_WORLD_HEIGHT / 4},
            new int[] { GAME_WORLD_WIDTH / 4, GAME_WORLD_HEIGHT * 3 / 4},
    };

    private static final int ASTEROID_PROTECTION_SCORE = 2;

    // global game time i.e. current frame
    private long time = 1;

    private int backgroundX = 0;

    private Map<String, String> players;

    private List<Asteroid> asteroids = new ArrayList<>();
    private Ship ship = null;
    private List<EffectActor> effects = new ArrayList<>();

    private Map<String, ShipCommand> roundCommands = new HashMap<>();

    private LinkedList<Ship> expiredShips = new LinkedList<>();

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
        AsteroidHitEffect.loadSprites(movieBuilder);
        AsteroidExplosionEffect.loadSprites(movieBuilder);
        ShipExplosionEffect.loadSprites(movieBuilder);
        ShipHitEffect.loadSprites(movieBuilder);

        Vector worldCenter = new Vector(GAME_WORLD_WIDTH / 2, GAME_WORLD_HEIGHT / 2);

        // load players
        String playerId = players.keySet().iterator().next();
        movieBuilder.addPlayer(playerId, players.get(playerId));
        Vector position = new Vector(
                (int) (INITIAL_POSITIONS[0][0] * (Math.random() * 0.2 + 0.9)),
                (int) (INITIAL_POSITIONS[0][1] * (Math.random() * 0.2 + 0.9)));
        ship = Ship.create(playerId,
                position, (((int) Math.toDegrees(position.headingTo(worldCenter)) - 90) + 360) % 360);

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

        // add actors

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

            if (ship == null || ship.expired(time))
                continue;

            if (shipCommand.isShield())
                ship.activateShield();

        }

        // update all actors
        updateActors();

        // draw
        buildFrame(movieBuilder);

        // do collision checks
        processAsteroidsCollisions();

        // add asteroids to world if it is too empty
        if (time % 1000 == 0 && asteroids.size() < MAX_NUMBER_ASTEROIDS)
            asteroids.add(generateAsteroid());

        // reset state
        roundCommands.clear();

        time++;
    }

    @Override
    public boolean isRunning() {
        return ship.isAlive() && time < 3000 ||
                ship.getKilledOn() > (time - FPS * 2);
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

        if (!ship.expired(time))
            ship.draw(time, movieBuilder);

        for (EffectActor effect: effects)
            effect.draw(time, movieBuilder);
    }

    /**
     * Update all actors based on expiration and velocity vector.
     */
    private void updateActors() {

        updateActors(asteroids);

        ship.onUpdate();
        if (!ship.expired(time))
            updateActorPosition(ship);

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
     * Process the existing collisions with asteroids in the game world.
     * Collisions with bullets already tested.
     */
    private void processAsteroidsCollisions() {

        for (int i = 0, size = asteroids.size(); i < size; i++) {
            Asteroid asteroid = asteroids.get(i);

            if (ship.expired(time))
                continue;

            if (asteroid.collides(ship)) {

                effects.add(asteroid.getHitEffect(time, ship));

                if (!ship.isShieldActive()) {

                    applyShipImpactOnAsteroid(ship, asteroid);

                    ship.hit(time, -1);
                    effects.add(ship.getExplosionEffect(time, asteroid));
                } else {
                    asteroid.hit(-1);

                    ship.addScorePoints(ASTEROID_PROTECTION_SCORE);

                    generateBabyAsteroids(asteroid, ship);
                    ship.setVelocity(new Vector(0, 0));
                    effects.add(asteroid.getExplosionEffect(time, ship));
                    return;
                }

                if (asteroid.hit(2)) {
                    generateBabyAsteroids(asteroid, ship);
                    effects.add(asteroid.getExplosionEffect(time, ship));
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

    /**
     * Generate and place asteroids on the game world at a minimum distance from the players.
     *
     * @return {@link Asteroid} the generated asteroid
     */
    private Asteroid generateAsteroid() {

        while (true) {

            // perform a test to check it is not too close to the player
            Vector position = new Vector(Math.random() * GAME_WORLD_WIDTH, Math.random() * GAME_WORLD_HEIGHT);

            if (ship.getPosition().distance(position) < MIN_ASTEROID_SHIP_DISTANCE)
                continue;

            Asteroid asteroid = Asteroid.createAsteroid(position);
            Vector velocity = new Vector(0, -asteroid.getVelocity().length()/(asteroids.size() + 1));
            int degrees = (((int) Math.toDegrees(asteroid.getPosition()
                    .headingTo(ship.getPosition())) - 90) + 360) % 360;
            velocity.rotate2d(Math.toRadians(degrees));

            asteroid.setVelocity(velocity);

            return asteroid;
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

        if (ship.isAlive()) {
            movieBuilder.addMessage(playerId, String.format(MSG_WINNER, name));
        } else {
            movieBuilder.addMessage(playerId, String.format(MSG_LOSER, name));
            throw new PlayerException(playerId, MooshakClassification.WRONG_ANSWER, "You need to survive!");
        }

        // set player submission status
        movieBuilder.setClassification(playerId, MooshakClassification.ACCEPTED);
        movieBuilder.setPoints(playerId, ship.getScore());

        // set the observations
        movieBuilder.setObservations(playerId, "You did it!");
    }
}
