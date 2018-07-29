package pt.up.fc.dcc.asura.bullseye;

import pt.up.fc.dcc.asura.builder.base.messaging.Command;
import pt.up.fc.dcc.asura.builder.base.messaging.PlayerAction;
import pt.up.fc.dcc.asura.builder.base.messaging.StateUpdate;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.builder.base.GameState;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Stores the current state of the Bullseye game
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class BullseyeState implements GameState {
    private static final String[] SHOT_ASSETS = {
            "hole-p1", "hole-p2"
    };
    private static final int NUMBER_OF_PLAYS = 5;
    private static final double BULLSEYE_DISTANCE = 1D*600; // 1 meter (600pixels = 1meter then 600*1 pixels)
    private static final int BOARD_WIDTH = 600;
    private static final int BOARD_HEIGHT = 600;
    private static final double[] BULLSEYE_CIRCLES_RADIUS = { 58.3, 114.58, 145.8, 202.1, 245.8, 300 };
    private static final int[] BULLSEYE_POINTS = { 50, 25, 20, 15, 10, 5 };

    private double[] startPosition;

    private Map<String, String> players;
    private List<String> playerOrder = new ArrayList<>();
    private String winner = null;

    private Map<String, List<String>> messages = null;

    // previous state
    private Map<String, List<double[]>> shots = new HashMap<>();
    private Map<String, Integer> scores = new HashMap<>();

    // current state
    private int currentPlay = 0;
    private int[] currentShot;
    private double[] currentShotPosition;
    private int currentShotScore;

    @Override
    public void prepare(GameMovieBuilder movieBuilder, Map<String, String> players) {

        this.players = players;

        for (String playerId : players.keySet()) {
            playerOrder.add(playerId);
            shots.put(playerId, new ArrayList<>());
            scores.put(playerId, 0);
        }

        Random rnd = new Random();

        startPosition = new double[2];
        startPosition[0] = rnd.nextDouble() * BOARD_WIDTH;
        startPosition[1] = rnd.nextDouble() * BOARD_HEIGHT;

        movieBuilder.setTitle(new BullseyeManager().getGameName());
        movieBuilder.setWidth(BOARD_WIDTH);
        movieBuilder.setHeight(BOARD_HEIGHT);
        movieBuilder.setBackground("bullseye.png");

        for (String playerId: players.keySet())
            movieBuilder.addPlayer(playerId, players.get(playerId));

        for (String shotAsset : SHOT_ASSETS)
            movieBuilder.addSprite(shotAsset, shotAsset + ".png");

        movieBuilder.setFps(1);
        movieBuilder.setSpriteAnchor(GameMovieBuilder.SpriteAnchor.BOTTOM_RIGHT);

        if (messages == null) {
            messages = new HashMap<>();

            for (String player : players.keySet()) {
                messages.put(player, new ArrayList<>());
            }
        }

        movieBuilder.addFrame();
        buildFrame(movieBuilder, null, null, 0);
    }

    public String getTurn() {
        return playerOrder.get(currentPlay % players.size());
    }

    @Override
    public void execute(GameMovieBuilder movieBuilder, String playerId, PlayerAction action) {

        Command command = action.getCommand();

        if (messages == null) {
            messages = new HashMap<>();

            for (String player : players.keySet()) {
                messages.put(player, new ArrayList<>());
            }
        }

        if (action.getMessages() != null)
            messages.get(playerId).addAll(action.getMessages());

        if (command != null) {
            Object[] args = command.getArgs();

            currentShot = new int[2];

            try {
                switch (command.getName()) {
                    case "SHOOT":
                        if (args.length != 2)
                            throw new IllegalArgumentException("SHOOT: two arguments expected");

                        try {
                            currentShot[0] = ((Double) args[0]).intValue();
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("SHOOT: horizontal angle expects an integer, " +
                                    "got '" + args[0] + "'");
                        }

                        if (currentShot[0] < -90 || currentShot[0] > 90)
                            throw new IllegalArgumentException("SHOOT: horizontal angle must be 0 <= a <= 180, " +
                                    "got '" + currentShot[0] + "'");

                        try {
                            currentShot[1] = ((Double) args[1]).intValue();
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("SHOOT: vertical angle expects an integer, " +
                                    "got '" + args[1] + "'");
                        }

                        if (currentShot[1] < -90 || currentShot[1] > 90)
                            throw new IllegalArgumentException("SHOOT: horizontal angle must be 0 <= a <= 180, " +
                                    "got '" + currentShot[1] + "'");

                        currentShotPosition = calculateShotPosition();
                        currentShotScore = calculateShotPoints(currentShotPosition);

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

        List<double[]> playerShots = shots.get(player);

        if (playerShots == null || playerShots.isEmpty())
            return null;

        double[] lastShot = playerShots.get(playerShots.size() - 1);

        return new StateUpdate("HIT", (int) lastShot[0] + " " + (int) lastShot[1]);
    }

    @Override
    public void endRound(GameMovieBuilder movieBuilder) {

        movieBuilder.addFrame();

        String playerId = getTurn();

        buildFrame(movieBuilder, playerId, currentShotPosition, currentShotScore);

        // add shot to previous shots
        List<double[]> playerShots = shots.get(playerId);
        playerShots.add(currentShotPosition);

        // update score
        int playerScore = scores.get(playerId) + currentShotScore;
        scores.put(playerId, playerScore);

        // update play turn
        currentPlay++;
    }

    /**
     * Calculate shot position in the bullseye plane
     *
     * @return shot position in the bullseye plane
     */
    private double[] calculateShotPosition() {

        double hypHoriz = Math.abs(currentShot[0]) == 90 ? 0 :
                BULLSEYE_DISTANCE / Math.cos(Math.toRadians(currentShot[0]));
        double oppHoriz = Math.sqrt(hypHoriz * hypHoriz - BULLSEYE_DISTANCE * BULLSEYE_DISTANCE);
        double bullseyePlaneX = currentShot[0] < 0 ? startPosition[0] - oppHoriz : startPosition[0] + oppHoriz;

        double hypVert = Math.abs(currentShot[1]) == 90 ? 0 :
                BULLSEYE_DISTANCE / Math.cos(Math.toRadians(currentShot[1]));
        double oppVert = Math.sqrt(hypVert * hypVert - BULLSEYE_DISTANCE * BULLSEYE_DISTANCE);
        double bullseyePlaneY = currentShot[1] < 0 ? startPosition[1] - oppVert : startPosition[1] + oppVert;

        return new double[] { bullseyePlaneX, bullseyePlaneY };
    }

    /**
     * Calculate points in the bullseye
     *
     * @param bullseyePos {@code double[]} position in the bullseye plane
     * @return points in the bullseye
     */
    private int calculateShotPoints(double[] bullseyePos) {

        double x = bullseyePos[0] - BOARD_WIDTH / 2;
        double y = bullseyePos[1] - BOARD_HEIGHT / 2;

        double prevRadius = 0;
        for (int i = 0; i < BULLSEYE_CIRCLES_RADIUS.length; i++) {

            double hyp = Math.sqrt(x*x + y*y);
            if (hyp >= prevRadius && hyp <= BULLSEYE_CIRCLES_RADIUS[i])
                return BULLSEYE_POINTS[i];
        }

        return 0;
    }

    /**
     * Build frame for movie
     *
     * @param movieBuilder {@link GameMovieBuilder} builder for movies
     * @param playerId ID of the player
     * @param shotPosition {@code double[]} position of the shot
     * @param score Score obtained with shot
     */
    private void buildFrame(GameMovieBuilder movieBuilder, String playerId, double[] shotPosition, int score) {

        movieBuilder.restoreFrame();

        if (playerId != null) {

            movieBuilder.addItem(SHOT_ASSETS[playerOrder.indexOf(playerId) % SHOT_ASSETS.length],
                    (int) shotPosition[0], (int) shotPosition[1], 0, 0.5);

            movieBuilder.setPoints(playerId, scores.get(playerId) + score);

            messages.get(playerId).add("Your shot at (" + (int) shotPosition[0] + ", "
                    + (int) shotPosition[1] + ") scored " + score + " points!");
        }

        outputMessages(movieBuilder);

        movieBuilder.saveFrame(true, false);
    }

    /**
     * Place messages generated during last moves in frame
     *
     * @param movieBuilder {@link GameMovieBuilder} builder for movies
     */
    private void outputMessages(GameMovieBuilder movieBuilder) {
        for (String playerId : messages.keySet()) {
            StringBuilder message = new StringBuilder();
            for (String line : messages.get(playerId)) {
                message.append(line);
                message.append("\n");
            }
            if (message.length() > 0)
                movieBuilder.addMessage(playerId, message.toString());
        }
    }

    @Override
    public boolean isRunning() {
        return currentPlay < NUMBER_OF_PLAYS * players.size() && winner == null;
    }

    @Override
    public void finalize(GameMovieBuilder movieBuilder) {

        if (messages == null) {
            messages = new HashMap<>();

            for (String player : players.keySet()) {
                messages.put(player, new ArrayList<>());
            }
        }

        movieBuilder.addFrame();

        List<String> sortedByScore = scores.keySet().stream()
                .sorted(Comparator.comparing(scores::get).reversed())
                .collect(Collectors.toList());
        if (sortedByScore.size() > 1 &&
                Objects.equals(scores.get(sortedByScore.get(0)), scores.get(sortedByScore.get(1))))
            winner = null;
        else
            winner = sortedByScore.get(0);

        buildFrame(movieBuilder, null, null, 0);

        for (String player: players.keySet()) {

            if (player.equals(winner))
                movieBuilder.setObservations(player, "You have won with a total score of " +
                        scores.get(player) + "! Congratulations!!");
            else if (winner != null)
                movieBuilder.setObservations(player, "You have lost. Your total score was " +
                        scores.get(player) + ", while " + players.get(winner) + " scored " +
                        scores.get(winner) + ".");
            else {

                if (Objects.equals(scores.get(sortedByScore.get(0)), scores.get(player)))
                    movieBuilder.setObservations(player, "You have drawn with a total score of " +
                            scores.get(player));
                else
                    movieBuilder.setObservations(player, "You have lost. Your total score was " +
                            scores.get(player) + ", while the winners scored " +
                            scores.get(sortedByScore.get(0)) + ".");
            }
        }
    }
}
