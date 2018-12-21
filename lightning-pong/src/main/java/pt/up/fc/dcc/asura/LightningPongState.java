package pt.up.fc.dcc.asura;

import pt.up.fc.dcc.asura.builder.base.messaging.Command;
import pt.up.fc.dcc.asura.builder.base.messaging.PlayerAction;
import pt.up.fc.dcc.asura.builder.base.messaging.StateUpdate;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.builder.base.GameState;
import pt.up.fc.dcc.asura.builder.base.movie.models.MooshakClassification;
import pt.up.fc.dcc.asura.messaging.CommandResult;
import pt.up.fc.dcc.asura.messaging.PlayerCommand;
import pt.up.fc.dcc.asura.messaging.PongUpdate;
import pt.up.fc.dcc.asura.models.*;
import pt.up.fc.dcc.asura.models.Character;
import pt.up.fc.dcc.asura.utils.CollisionProcessor;
import pt.up.fc.dcc.asura.utils.SpriteManager;
import pt.up.fc.dcc.asura.utils.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder.SpriteAnchor;

/**
 * Stores the current state of the Lightning Pong game
 *
 * @author José C. Paiva <code>josepaiva94@gmail.com</code>
 */
public class LightningPongState implements GameState {
    public static final int GAME_WORLD_WIDTH = 700;
    public static final int GAME_WORLD_HEIGHT = 500;

    private static final String MSG_WINNER = "Congratulations, %s. You have won!";
    private static final String MSG_LOSER = "%s, your score was not good enough! Keep calm and get back stronger.";
    private static final String MSG_TIE = "It's a tie! You are as good as your opponent or as bad as him.";

    private static final String BACKGROUND_SPRITE = "bg.png";

    private static final int FPS = 60;

    private static final int GOAL_LINE_OFFSET = 35;
    private static final int PADDLE_LINE_OFFSET = 50;

    private static final long GAME_MAX_TIME = 10000;
    private static final int GAME_MAX_SCORE = 5;

    // the amount of influence the ball's position against the paddle
    // has on the X speed. This number must be between 0 and 1.
    private static final double BALL_PADDLE_INFLUENCE_Y = 0.75;

    private static final double BALL_SPEED_INCREASE = 0.1;

    private static final long LASER_FREEZE_TIME = 60;
    private static final long POWER_UP_BONUS_TIME = 100;

    private static final long HEADER_LINE_DURATION = 350;

    private int time = 0;
    private boolean maxScore = false;

    private Character score1st;
    private Character score2nd;

    private List<Player> players = new ArrayList<>();

    private List<Character> headerLine = new ArrayList<>();

    private Ball ball;

    private Map<String, List<Laser>> lasers = new HashMap<>();

    private List<PowerUp> powerUps = new ArrayList<>();

    private Map<String, PlayerCommand> commands = new HashMap<>();
    private Map<String, CommandResult> commandResults = new HashMap<>();

    @Override
    public void prepare(GameMovieBuilder builder, String title, Map<String, String> playerNames) {

        builder.setTitle(title);
        builder.setWidth(GAME_WORLD_WIDTH);
        builder.setHeight(GAME_WORLD_HEIGHT);
        builder.setBackground(BACKGROUND_SPRITE);
        builder.setFps(FPS);
        builder.setSpriteAnchor(SpriteAnchor.CENTER);

        // load sprites
        SpriteManager.load(builder);

        // load players
        for (String playerId: playerNames.keySet())
            builder.addPlayer(playerId, playerNames.get(playerId));

        // place objects
        for (String playerId: playerNames.keySet()) {
            Paddle paddle = new Paddle(
                    new Vector(players.isEmpty() ? PADDLE_LINE_OFFSET :
                            GAME_WORLD_WIDTH - PADDLE_LINE_OFFSET,
                            GAME_WORLD_HEIGHT >> 1),
                    new Vector(0,0), players.size());
            players.add(new Player(playerId, playerNames.get(playerId), paddle));
            lasers.put(playerId, new ArrayList<>());
        }

        ball = new Ball(new Vector(GAME_WORLD_WIDTH >> 1, GAME_WORLD_HEIGHT >> 1));

        headerLine = generateString(
                String.format("%s vs %s", players.get(0).getPlayerName(), players.get(1).getPlayerName()),
                GAME_WORLD_WIDTH/2, 25);

        updateScoreSprites();
    }

    @Override
    public void execute(GameMovieBuilder builder, String playerId, PlayerAction action) {

        commandResults.remove(playerId);

        Command command = action.getCommand();

        if (command != null) {
            Object[] args = command.getArgs();

            try {
                switch (command.getName()) {
                    case "PADDLE":
                        if (args.length != 3)
                            throw new IllegalArgumentException("PADDLE: three arguments expected" +
                                    " (horizontal, vertical, fire)");

                        int horizontal = command.getAsInt(0);
                        int vertical = command.getAsInt(1);
                        boolean fire = command.getAsBoolean(2);

                        commands.put(playerId, new PlayerCommand(
                                vertical, horizontal, fire, action.getMessages()));

                        break;
                    default:
                        throw new IllegalArgumentException(command.getName());
                }
            } catch (IllegalArgumentException e) {
                builder.addFrame();
                builder.wrongAnswer(playerId, e.getMessage());
                throw e;
            }
        }
    }

    @Override
    public StateUpdate getStateUpdateFor(String playerId) {

        PongUpdate update = new PongUpdate();

        update.setBall((int) ball.getPosition().getX(), (int) ball.getPosition().getY());

        for (Player player: players) {

            Paddle paddle = player.getPaddle();
            if (player.getPlayerId().equals(playerId)) {
                update.setMe(
                        (int) paddle.getPosition().getX(),
                        (int) paddle.getPosition().getY(),
                        paddle.getLargerTime(),
                        paddle.getFasterTime(),
                        paddle.getFrozenTime());
            } else {
                update.setOpponent(
                        (int) paddle.getPosition().getX(),
                        (int) paddle.getPosition().getY(),
                        paddle.getLargerTime() > 0,
                        paddle.getFasterTime() > 0,
                        paddle.getFrozenTime() > 0);
            }
        }

        update.setResult(commandResults.get(playerId) == null ? CommandResult.SUCCESS :
                commandResults.get(playerId));

        return new StateUpdate("PONG", update);
    }

    @Override
    public void endRound(GameMovieBuilder builder) {

        // process commands
        processPlayerCommands();

        // update the actors with velocity and check expiration
        updateActors();

        // check if there is a winner
        if (checkGoalAndReset()) {

        } else {

            // process collisions
            for (Player player: players) {
                if (player.getPaddle().collides(ball))
                    processPaddleBallCollision(player.getPaddle(), ball);

                for (PowerUp powerUp: powerUps)
                    if (player.getPaddle().collides(powerUp))
                        applyPowerUp(powerUp, player, player.getPaddle());

                for (Laser laser: lasers.get(player.getPlayerId())) {

                    if (laser.collides(ball))
                        processLaserBallCollision(laser, ball);

                    for (Player opp: players)
                        if (!player.getPlayerId().equals(opp.getPlayerId()))
                            if (opp.getPaddle().collides(laser))
                            processPaddleLaserCollision(opp.getPaddle(), laser);

                    for (PowerUp powerUp: powerUps) {
                        if (laser.collides(powerUp))
                            applyPowerUp(powerUp, player, laser);
                    }
                }
            }

            // generate power-ups
            boolean generateGoodPowerUp = powerUps.size() < 2 && time % 5 == 0 && Math.random() < 0.01;
            boolean generateBadPowerUp = powerUps.size() < 2 && time % 7 == 0 && Math.random() < 0.005;

            if (generateGoodPowerUp)
                powerUps.add(new PowerUp((int) Math.round(Math.random())));
            else if (generateBadPowerUp)
                powerUps.add(new PowerUp(2));
        }

        // build the frame
        buildFrame(builder);

        // reset state
        commands.clear();

        time++;
    }

    @Override
    public boolean isRunning() {
        return !maxScore && time < GAME_MAX_TIME;
    }

    @Override
    public void finalize(GameMovieBuilder builder) {

        builder.saveFrame();

        builder.addFrame();

        builder.restoreFrame();

        Player winner = null, loser = null;
        if (players.get(0).getScore() > players.get(1).getScore()) {
            winner = players.get(0);
            loser = players.get(1);
        } else if (players.get(0).getScore() < players.get(1).getScore()) {
            winner = players.get(1);
            loser = players.get(0);
        }

        if (winner == loser) {
            builder.addMessage(players.get(0).getPlayerId(), MSG_TIE);
            builder.addMessage(players.get(1).getPlayerId(), MSG_TIE);
            winner = players.get(0); loser = players.get(1);
        } else {
            builder.addMessage(winner.getPlayerId(), String.format(MSG_WINNER, winner.getPlayerName()));
            builder.addMessage(loser.getPlayerId(), String.format(MSG_LOSER, loser.getPlayerName()));
        }

        builder.setClassification(winner.getPlayerId(), MooshakClassification.ACCEPTED);
        builder.setClassification(loser.getPlayerId(), MooshakClassification.ACCEPTED);
        builder.setPoints(winner.getPlayerId(), winner.getScore());
        builder.setPoints(loser.getPlayerId(), loser.getScore());
    }

    private void processPlayerCommands() {

        for (Player player: players) {

            PlayerCommand command = commands.get(player.getPlayerId());

            CommandResult result = CommandResult.SUCCESS;

            if (player.getPaddle().getFrozenTime() > 0) {
                result =  CommandResult.FAILURE_FROZEN;
            } else if (command.isFire()) {
                Laser laser = player.getPaddle().fire();

                if (laser == null)
                    result = CommandResult.FAILURE_RECHARGING;
                else
                    lasers.get(player.getPlayerId()).add(laser);
            } else if (command.getVertical() == 1) {
                if (!player.getPaddle().up())
                    result = CommandResult.FAILURE_OUT_OF_BOUNDS;
            } else if (command.getVertical() == 2) {
                if (!player.getPaddle().down())
                    result = CommandResult.FAILURE_OUT_OF_BOUNDS;
            }

            commandResults.put(player.getPlayerId(), result);
        }
    }

    /**
     * Build a frame of the game
     *
     * @param builder {@link GameMovieBuilder} the game movie builder
     */
    private void buildFrame(GameMovieBuilder builder) {

        // add frame
        builder.addFrame();

        // add messages
        for (Player player: players) {
            PlayerCommand shipCommand = commands.get(player.getPlayerId());

            List<String> messages = shipCommand.getMessages();
            if (messages != null) {
                StringBuilder msgBuilder = new StringBuilder();
                for (String message: messages) {
                    msgBuilder.append(message).append("\n");
                }
                builder.addMessage(player.getPlayerId(), msgBuilder.toString());
            }
        }

        // draw score
        score1st.draw(builder);
        score2nd.draw(builder);

        // draw header line
        if (time < HEADER_LINE_DURATION) {

            for (Character character: headerLine)
                character.draw(builder);
        }

        // draw the actors
        drawActors(builder);
    }

    /**
     * Update all actors based on expiration and velocity vector.
     */
    private void updateActors() {

        ball.onUpdate();

        for (Player player: players)
            player.getPaddle().onUpdate();

        for (String playerId: lasers.keySet()) {

            List<Laser> playerLasers = new ArrayList<>(lasers.get(playerId));
            for (Laser laser: playerLasers) {

                if (laser.expired())
                    lasers.get(playerId).remove(laser);
                else
                    laser.onUpdate();
            }
        }

        List<PowerUp> roundPowerUps = new ArrayList<>(powerUps);
        for (PowerUp powerUp: roundPowerUps) {

            if (powerUp.expired())
                powerUps.remove(powerUp);
            else
                powerUp.onUpdate();
        }
    }

    /**
     * Draw all actors on the frame.
     *
     * @param builder {@link GameMovieBuilder} the game movie builder
     */
    private void drawActors(GameMovieBuilder builder) {

        for (Player player: players)
            player.getPaddle().draw(builder);

        ball.draw(builder);

        for (String playerId: lasers.keySet())
            for (Laser laser: lasers.get(playerId))
                laser.draw(builder);

        for (PowerUp powerUp: powerUps)
            powerUp.draw(builder);
    }

    /**
     * Process a collision between the paddle and the ball
     *
     * @param paddle {@link Paddle} the paddle
     * @param ball {@link Ball} the ball
     */
    private void processPaddleBallCollision(Paddle paddle, Ball ball) {

        Vector dist = CollisionProcessor.distanceRectCircle(
                paddle.getPosition().getX(), paddle.getPosition().getY(),
                paddle.width(), paddle.height(),
                ball.getPosition().getX(), ball.getPosition().getY(),
                ball.width()
        );

        if (ball.getVelocity().dot(dist) < 0) {
            double posY = (ball.getPosition().getY() - paddle.getPosition().getY())
                    / (paddle.height() / 2);

            double speedXY = ball.getVelocity().length()
                    + BALL_SPEED_INCREASE * paddle.getVelocity().getY();

            double speedY = speedXY * posY * BALL_PADDLE_INFLUENCE_Y;

            double speedX = Math.sqrt(speedXY * speedXY - speedY * speedY)
                    * (ball.getVelocity().getX() > 0 ? -1 : 1);

            // update ball velocity
            ball.setVelocity(new Vector(speedX, speedY));
        }

        double penetrationDepth = ball.width() - dist.length();

        Vector penetrationVector = dist.normalize().scale(penetrationDepth);

        ball.getPosition().add(penetrationVector.scale(-1));
    }

    /**
     * Process a collision between a paddle and a laser
     *
     * @param paddle {@link Paddle} the paddle
     * @param laser {@link Laser} the laser
     */
    private void processPaddleLaserCollision(Paddle paddle, Laser laser) {

        paddle.freeze(LASER_FREEZE_TIME);
        laser.onHit(paddle, 1);
    }

    /**
     * Process a collision between a laser and the ball
     *
     * @param laser {@link Laser} the paddle
     * @param ball {@link Ball} the ball
     */
    private void processLaserBallCollision(Laser laser, Ball ball) {

        ball.freeze(LASER_FREEZE_TIME/2);
        laser.onHit(ball, 1);
    }

    /**
     * Apply a power-up caught by a player
     *
     * @param powerUp {@link PowerUp} power-up to apply
     * @param player {@link Player} player that caught the power-up
     * @param actor {@link Actor} laser or paddle which hit the power-up
     */
    private void applyPowerUp(PowerUp powerUp, Player player, Actor actor) {

        switch (powerUp.getBonus()) {

            case LARGER_PADDLE:
                player.getPaddle().setLarger(POWER_UP_BONUS_TIME);
                break;
            case FASTER_PADDLE:
                player.getPaddle().setFaster(POWER_UP_BONUS_TIME);
                break;
            case FASTER_BALL:
                ball.setFaster(POWER_UP_BONUS_TIME);
                break;
        }

        powerUp.onHit(actor, 1);
    }

    /**
     * Check if there is a goal. If yes, reset ball.
     *
     * @return {@code true} if there was a goal, {@code false} otherwise
     */
    private boolean checkGoalAndReset() {

        int winnerIndex;
        if (ball.getPosition().getX() < GOAL_LINE_OFFSET)
            winnerIndex = 1;
        else if (ball.getPosition().getX() > GAME_WORLD_WIDTH - GOAL_LINE_OFFSET)
            winnerIndex = 0;
        else
            return false;

        Player winner = players.get(winnerIndex);
        winner.addPoint();

        if (winner.getScore() >= GAME_MAX_SCORE)
            maxScore = true;
        else {
            ball = new Ball(
                    new Vector(GAME_WORLD_WIDTH >> 1, GAME_WORLD_HEIGHT >> 1),
                    winnerIndex == 0);
        }

        updateScoreSprites();

        return true;
    }

    private void updateScoreSprites() {

        score1st = new Character(
                new Vector((double) GAME_WORLD_WIDTH * 2 / 5, (double) GAME_WORLD_HEIGHT / 6),
                (char) (players.get(0).getScore() + '0') - 32, -1, 0.75);
        score2nd = new Character(
                new Vector((double) GAME_WORLD_WIDTH * 3 / 5, (double) GAME_WORLD_HEIGHT / 6),
                (char) (players.get(1).getScore() + '0') - 32, -1, 0.75);
    }

    private List<Character> generateString(String s, int x, int y) {

        List<Character> characters = new ArrayList<>();

        char[] cs = s.toCharArray();

        int currentX = x - cs.length * 20 / 2;
        for (char c: cs) {
            Character character = new Character(new Vector(currentX, y),
                    (c) - 32, - 1, 0.5);
            characters.add(character);
            currentX += 20;
        }

        return characters;
    }
}
