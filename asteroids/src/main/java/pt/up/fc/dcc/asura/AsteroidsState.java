package pt.up.fc.dcc.asura;

import pt.up.fc.dcc.asura.builder.base.messaging.Command;
import pt.up.fc.dcc.asura.builder.base.messaging.PlayerAction;
import pt.up.fc.dcc.asura.builder.base.messaging.StateUpdate;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.builder.base.GameState;
import pt.up.fc.dcc.asura.builder.base.movie.models.MooshakClassification;
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
    private static final double RANGE_BULLET_WARN = 50;
    private static final double RANGE_SHIP_WARN = 50;

    private static final int HIT_ASTEROID_SCORE = 2;
    private static final int HIT_SHIP_SCORE = 5;
    private static final int DESTROYED_ASTEROID_SCORE = 4;
    private static final int DESTROYED_SHIP_SCORE = 10;
    private static final int BULLET_PROTECTION_SCORE = 4;
    private static final int SHOT_ON_PROTECTION_SCORE = 2;
    private static final int ASTEROID_PROTECTION_SCORE = 2;
    private static final int HEALTH_5_POINT_SCORE = 2;

    // global game time i.e. current frame
    public static int time = 1;

    private int backgroundX = 0;

    private Map<String, String> players;

    private List<Asteroid> asteroids = new ArrayList<>();
    private Map<String, Ship> ships = new HashMap<>();
    private Map<String, List<Bullet>> bullets = new HashMap<>();
    private Map<String, List<Bomb>> bombs = new HashMap<>();
    private List<EffectActor> effects = new ArrayList<>();

    private Map<String, ShipCommand> roundCommands = new HashMap<>();

    private LinkedList<Ship> expiredShips = new LinkedList<>();
    private Map<String, LinkedList<Bullet>> bulletResults = new HashMap<>();

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
        Bomb.loadSprites(movieBuilder);
        BombExplosionEffect.loadSprites(movieBuilder);

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
            bombs.put(playerId, new LinkedList<>());
            bulletResults.put(playerId, new LinkedList<>());
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
                    case "SHIP":
                        if (args.length != 5)
                            throw new IllegalArgumentException("SHIP: four arguments expected" +
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

        // bombs within a range of 50 pixels
        for (String playerId: bombs.keySet()) {

            if (playerId.equals(player))
                continue;

            List<Bomb> playerBombs = bombs.get(playerId);
            for (Bomb bomb: playerBombs) {

                if (ship.withinRange(bomb, RANGE_BULLET_WARN)) {
                    update.addActor(ShipUpdate.Type.BULLET,
                            (int) bomb.getPosition().getX(),
                            (int) bomb.getPosition().getY());
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
                    ship.fireSecondary(bombs.get(playerId));
                    break;
            }
        }

        // update all actors
        updateActors();

        // draw
        buildFrame(movieBuilder);

        // do collision checks
        processBulletCollisions();
        processBombCollisions();
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
        return ships.size() > 1 && time < 10000 ||
                expiredShips.getLast().getKilledOn() > (time - FPS * 2);
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
            asteroid.draw(movieBuilder);

        for (List<Bullet> playerBullets: bullets.values())
            for (Bullet bullet: playerBullets)
                bullet.draw(movieBuilder);

        for (List<Bomb> playerBombs: bombs.values())
            for (Bomb bomb: playerBombs)
                bomb.draw(movieBuilder);

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

        for (List<Bomb> playerBombs: bombs.values())
            updateActors(playerBombs);

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
                } else if (actor instanceof Bomb) {
                    if (!((Bomb) actor).exploded())
                        explodeBomb((Bomb) actor, null);
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
                        effects.add(bullet.getHitEffect(asteroid));

                        if (asteroid.hit(bullet.power())) {
                            bullet.setResult(BulletResult.DESTROYED_ASTEROID);

                            owner.addScorePoints(DESTROYED_ASTEROID_SCORE);

                            generateBabyAsteroids(asteroid, bullet);

                            // add animation
                            effects.add(asteroid.getExplosionEffect(bullet));
                        } else {
                            bullet.setResult(BulletResult.HIT_ASTEROID);

                            owner.addScorePoints(HIT_ASTEROID_SCORE);

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
                        if (ship.isShieldActive()) {
                            bullet.setResult(BulletResult.HIT_SHIELD);

                            ship.addScorePoints(BULLET_PROTECTION_SCORE);
                            owner.addScorePoints(SHOT_ON_PROTECTION_SCORE);

                            continue;
                        }

                        if (ship.hit(bullet.power())) {
                            bullet.setResult(BulletResult.DESTROYED_SHIP);

                            owner.addScorePoints(DESTROYED_SHIP_SCORE);

                            // add animation
                            effects.add(ship.getExplosionEffect(bullet));
                        } else {
                            bullet.setResult(BulletResult.HIT_SHIP);

                            owner.addScorePoints(HIT_SHIP_SCORE);

                            // add animation
                            effects.add(ship.getHitEffect(bullet));
                        }
                    }
                }
            }
        }
    }

    /**
     * Process the bomb collisions with other objects in the game world.
     * Collisions with bombs already tested.
     */
    private void processBombCollisions() {

        for (String playerId: bombs.keySet()) {
            Ship owner = findPlayerShip(playerId);
            if (owner == null)
                continue;

            List<Bomb> playerBombs = bombs.get(playerId);
            for (Bomb bomb: playerBombs) {
                if (bomb.exploded())
                    continue;

                for (int i = 0, size = asteroids.size(); i < size; i++) {
                    Asteroid asteroid = asteroids.get(i);

                    if (bomb.collides(asteroid))
                        explodeBomb(bomb, asteroid);
                }

                for (Ship ship: ships.values()) {

                    // check for other ships
                    if (bomb.collides(ship))
                        explodeBomb(bomb, ship);
                }
            }
        }
    }

    private void explodeBomb(Bomb bomb, Actor actor) {

        bomb.hit(-1);

        // add animation
        effects.add(bomb.getHitEffect(actor));

        Ship bombOwner = findPlayerShip(bomb.getPlayerId());
        if (bombOwner == null)
            return;

        for (int i = 0, size = asteroids.size(); i < size; i++) {
            Asteroid asteroid = asteroids.get(i);

            if (bomb.withinRange(asteroid, bomb.effectRadius())) {

                double distance = Math.max((bomb.getPosition().distance(asteroid.getPosition())
                        - bomb.radius() - asteroid.radius()), 10);

                if (asteroid.hit(bomb.power() * 10/distance)) {
                    bombOwner.addScorePoints(DESTROYED_ASTEROID_SCORE);

                    generateBabyAsteroids(asteroid, bomb);

                    // add animation
                    effects.add(asteroid.getExplosionEffect(bomb));
                } else {
                    bombOwner.addScorePoints(HIT_ASTEROID_SCORE);

                    applyBulletImpactOnAsteroid(bomb, asteroid);

                    // add animation
                    effects.add(asteroid.getHitEffect(bomb));
                }
            }
        }

        for (Ship ship: ships.values()) {

            if (bomb.withinRange(ship, bomb.effectRadius())) {

                double distance = Math.max((bomb.getPosition().distance(ship.getPosition())
                        - bomb.radius() - ship.radius()), 10);

                if (ship.isShieldActive())
                    continue;

                if (ship.hit(bomb.power() * 10/distance)) {
                    bombOwner.addScorePoints(DESTROYED_SHIP_SCORE);

                    // add animation
                    effects.add(ship.getExplosionEffect(bomb));
                } else {
                    bombOwner.addScorePoints(HIT_SHIP_SCORE);

                    // add animation
                    effects.add(ship.getHitEffect(bomb));
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

                        ship.addScorePoints(ASTEROID_PROTECTION_SCORE);

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

        // set observations based on behaviour
        StringBuilder observationsBuilder = new StringBuilder();
        if (ship.isAlive()) {
            ship.addScorePoints((int) (Math.floor(ship.getHealth() / 5) * HEALTH_5_POINT_SCORE));

            // set message
            if (ships.size() > 1)
                movieBuilder.addMessage(playerId, String.format(MSG_MULTIPLE_WINNER, name));
            else
                movieBuilder.addMessage(playerId, String.format(MSG_WINNER, name, name));

            // calculate observations
            if (ship.getHealth() >= Ship.MAX_HEALTH - HEALTH_TOLERANCE) {
                observationsBuilder.append("Your ship remained intact, that's amazing. Try other opponents, maybe" +
                        " these were newbies thrown into space to save on food.");
            } else if (ship.getHealth() <= HEALTH_TOLERANCE * 2) {
                observationsBuilder.append("Your ship was not destroyed, but you were lucky.");

                if (ship.getEnergy() > ENERGY_TOLERANCE * 2) {
                    observationsBuilder
                            .append(" ")
                            .append("Use your energy to protect yourself from the danger of the space with " +
                                    "the shield. It's better to trust in your mind than in luck.");
                } else {
                    observationsBuilder
                            .append(" ")
                            .append("Are you sure that you are using your energy efficiently? Use it, " +
                                    "but use it wisely.");
                }
            } else {
                observationsBuilder.append("You haven't died and you were not so close to death. But remember," +
                        " this was just a battle and there are a number of enemies out there to fear.");
            }
        } else {
            movieBuilder.addMessage(playerId, String.format(MSG_LOSER, name));

            // calculate observations
            if (ship.getEnergy() > ENERGY_TOLERANCE * 2) {
                observationsBuilder.append("You should think about how to use your energy better," +
                        " particularly on deciding when you should activate the shield. You got killed" +
                        " with enough energy to activate it.");
            } else {
                observationsBuilder.append("Are you sure that you are using your energy efficiently? " +
                        "You should keep enough energy for activating the shield in case of emergency.");
            }
        }

        // shots
        double fireCountScoreRatio = (double) ship.getScore() / ship.getFireCount();
        if (fireCountScoreRatio >= RATIO_FIRE_COUNT_SCORE) {
            observationsBuilder.append(" ").append("About your shots, you are doing good.");
        } else if (fireCountScoreRatio <= RATIO_FIRE_COUNT_SCORE / 2) {
            observationsBuilder.append(" ").append("About your shots, it seems that you are doing them randomly. " +
                    "Shooting is all about maths.");
        }

        movieBuilder.addMessage(playerId, ship.getScore() + "--");

        // set player submission status
        movieBuilder.setClassification(playerId, MooshakClassification.ACCEPTED);
        movieBuilder.setPoints(playerId, ship.getScore());

        // set the observations
        movieBuilder.setObservations(playerId, observationsBuilder.toString());
    }
}
