package pt.up.fc.dcc.asura.tictactoe;

import pt.up.fc.dcc.asura.builder.base.exceptions.PlayerException;
import pt.up.fc.dcc.asura.builder.base.messaging.PlayerAction;
import pt.up.fc.dcc.asura.builder.base.messaging.StateUpdate;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.builder.base.GameState;
import pt.up.fc.dcc.asura.builder.base.movie.models.MooshakClassification;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Stores the current state of the Tic Tac Toe game
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class TicTacToeState implements GameState {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final String SPRITE_BG = "board.png";
    private static final String SPRITE_X = "x.png";
    private static final String SPRITE_O = "o.png";
    private static final int[] POSITIONS = new int[] {0, 210, 415};
    private static final int[][] WINNING_COMBOS = new int[][] {
            new int[] {1, 2, 3}, new int[] {4, 5, 6}, new int[] {7, 8, 9},
            new int[] {1, 5, 9}, new int[] {3, 5, 7},
    };

    private Map<String, String> players;
    private Map<String, String> playerSymbols = new HashMap<>();

    private char[] board = "          ".toCharArray();
    private int lastPlayed = 0;
    private int round = 0;

    @Override
    public void prepare(GameMovieBuilder movieBuilder, String title, Map<String, String> players) {

        this.players = players;

        movieBuilder.setTitle(title);
        movieBuilder.setFps(1);
        movieBuilder.setHeight(HEIGHT);
        movieBuilder.setWidth(WIDTH);
        movieBuilder.setSpriteAnchor(GameMovieBuilder.SpriteAnchor.TOP_LEFT);
        movieBuilder.setBackground(SPRITE_BG);
        movieBuilder.addSprite("X", SPRITE_X);
        movieBuilder.addSprite("O", SPRITE_O);

        for (String playerId: players.keySet()) {
            movieBuilder.addPlayer(playerId, players.get(playerId));
        }

        movieBuilder.addFrame();
    }

    @Override
    public void execute(GameMovieBuilder movieBuilder, String playerId, PlayerAction action) {

        switch (action.getCommand().getName()) {
            case "SYMBOL":
                String symbol = action.getCommand().getAsString(0);
                if (playerSymbols.containsKey(playerId))
                    throw new PlayerException(playerId, MooshakClassification.WRONG_ANSWER,
                            "Cannot change symbol during game.");
                playerSymbols.put(playerId, symbol);
                break;
            case "PLAY":
                int position = action.getCommand().getAsInt(0);
                if (position < 1 || position > 9)
                    throw new PlayerException(playerId, MooshakClassification.WRONG_ANSWER,
                            "Invalid position.");
                if (board[position] != ' ')
                    throw new PlayerException(playerId, MooshakClassification.WRONG_ANSWER,
                            "Already used position.");
                Logger.getLogger("").severe(""+position);
                board[position] = playerSymbols.get(playerId).charAt(0);
                lastPlayed = position;
                break;
        }
    }

    @Override
    public StateUpdate getStateUpdateFor(String player) {
        return new StateUpdate("LAST_PLAYED", lastPlayed);
    }

    @Override
    public void endRound(GameMovieBuilder movieBuilder) {
        movieBuilder.addFrame();

        movieBuilder.restoreFrame();

        movieBuilder.addItem(String.valueOf(board[lastPlayed]), POSITIONS[(lastPlayed - 1) % 3],
                POSITIONS[(lastPlayed - 1) / 3]);

        movieBuilder.saveFrame(true, false);

        round++;
    }

    @Override
    public boolean isRunning() {

        if (round >= 9)
            return true;

        return !hasWinner();
    }

    private boolean hasWinner() {

        for (int[] combo: WINNING_COMBOS)
            if (board[combo[0]] != ' ' &&
                    board[combo[0]] == board[combo[1]] && board[combo[1]] == board[combo[2]])
                return true;

        return false;
    }

    @Override
    public void finalize(GameMovieBuilder movieBuilder) {

        String winnerSymbol = hasWinner() ? String.valueOf(board[lastPlayed]) : null;

        movieBuilder.addFrame();
        movieBuilder.restoreFrame();
        for (String playerId: players.keySet()) {
            if (winnerSymbol == null) {
                movieBuilder.setObservations(playerId, "It's a tie!");
                movieBuilder.setPoints(playerId, 50);
            } else if (winnerSymbol.equals(playerSymbols.get(playerId))) {
                movieBuilder.setObservations(playerId, "WINNER!");
                movieBuilder.setPoints(playerId, 100);
            } else {
                movieBuilder.setObservations(playerId, "You've lost :(");
                movieBuilder.setPoints(playerId, 0);
            }
            movieBuilder.setClassification(playerId, MooshakClassification.ACCEPTED);
        }
    }
}
