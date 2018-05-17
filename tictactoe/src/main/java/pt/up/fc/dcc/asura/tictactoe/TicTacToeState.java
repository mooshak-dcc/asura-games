package pt.up.fc.dcc.asura.tictactoe;

import pt.up.fc.dcc.asura.builder.base.messaging.Command;
import pt.up.fc.dcc.asura.builder.base.messaging.PlayerAction;
import pt.up.fc.dcc.asura.builder.base.messaging.StateUpdate;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.builder.base.GameState;
import pt.up.fc.dcc.asura.builder.base.movie.models.MooshakClassification;

import java.util.*;

/**
 * Stores the current state of the TicTacToe game
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class TicTacToeState implements GameState {

    private static final int FRAMES_PER_SECOND = 1;
    private static final int WIN_POINTS = 100;
    private static final int DRAW_POINTS = 50;
    private static final int LOSE_POINTS = 0;
    private static final char[] PIECES = new char[]{'X', 'O'};

    private char board[];
    private int count = 0;
    private String winner = null;
    private Map<String, List<String>> messages = new HashMap<>();

    private HashMap<String, TicTacToePiece> playersPieces = new HashMap<>();
    private String playerX = null;
    private String playerO = null;
    private int roundMove = -1;
    private String roundPlayer;

    @Override
    public void prepare(GameMovieBuilder movieBuilder, Map<String, String> players) {

        board = new char[10];

        for (int i = 0; i < 10; i++)
            board[i] = ' ';
        count = 0;

        int rnd = (int) Math.round(Math.random());
        for (String playerId : players.keySet()) {
            TicTacToePiece piece = new TicTacToePiece(PIECES[rnd]);
            piece.points = LOSE_POINTS;
            piece.classification = MooshakClassification.ACCEPTED;
            piece.observations = "";

            if (rnd == 1) {
                rnd = 0;
                playerO = playerId;
            } else {
                rnd = 1;
                playerX = playerId;
            }

            playersPieces.put(playerId, piece);
        }

        movieBuilder.setTitle("Tic Tac Toe Game");
        movieBuilder.setBackground("board-3x3.png");
        movieBuilder.setWidth(600);
        movieBuilder.setHeight(600);

        for (String playerId : players.keySet())
            movieBuilder.addPlayer(playerId, players.get(playerId));

        for (String name : Arrays.asList("o_piece", "x_piece"))
            movieBuilder.addSprite(name, name + ".png");

        movieBuilder.setFps(FRAMES_PER_SECOND);
        movieBuilder.setSpriteAnchor(GameMovieBuilder.SpriteAnchor.TOP_LEFT);
    }

    @Override
    public void execute(GameMovieBuilder movieBuilder, String playerId, PlayerAction action) {

        roundPlayer = playerId;

        Command command = action.getCommand();

        if (messages == null) {
            messages = new HashMap<>();

            for (String player : playersPieces.keySet()) {
                messages.put(player, new ArrayList<>());
            }
        }

        if (action.getMessages() != null)
            messages.put(playerId, action.getMessages());

        if (command != null) {
            Object[] args = command.getArgs();

            try {
                switch (command.getName()) {
                    case "PLAY":
                        if (args.length != 1)
                            throw new IllegalArgumentException("PLAY: one argument expected");

                        try {
                            roundMove = ((Double) args[0]).intValue();
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException("PLAY: integer argument expected" + e.getMessage());
                        }

                        if (roundMove < 1 || roundMove > 9 || !valid(roundMove))
                            throw new IllegalArgumentException("PLAY: invalid move=" + roundMove +
                                    " (1 <= move <= 9)");

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
        return new StateUpdate("BOARD", board);
    }

    @Override
    public void endRound(GameMovieBuilder movieBuilder) {
        TicTacToePiece player = playersPieces.get(roundPlayer);

        movieBuilder.addFrame();

        board[roundMove] = player.piece;
        count++;

        if (wins(player.piece)) {
            winner = roundPlayer;

            playersPieces.get(winner).observations = "You've won!";
            playersPieces.get(winner).points = WIN_POINTS;

            messages.get(winner).add("You've won!");

            String loser = winner.equals(playerX) ? playerO : playerX;
            playersPieces.get(loser).observations = "The bot seems ok, but it has lost!";
            playersPieces.get(loser).points = LOSE_POINTS;

            messages.get(loser).add("The bot seems ok, but it has lost!");
        }

        buildFrame(movieBuilder);

        outputMessages(movieBuilder);
    }

    public void buildFrame(GameMovieBuilder movieBuilder) {

        for (int i = 1; i < board.length; i++) {
            if (board[i] == ' ')
                continue;

            int x = (160 + 27) * ((i - 1) % 3);
            int y = (160 + 27) * ((i - 1) / 3);

            movieBuilder.addItem((board[i] + "").toLowerCase() + "_piece", x, y);
        }
    }

    /**
     * Place messages generated during last moves in frame
     *
     * @param movieBuilder {@link GameMovieBuilder} movie builder
     */
    private void outputMessages(GameMovieBuilder movieBuilder) {
        for (String playerId : messages.keySet()) {
            String message = null;
            for (String line : messages.get(playerId)) {
                if (message == null)
                    message = "";
                else
                    message += "\n";
                message += line;
            }
            if (message != null)
                movieBuilder.addMessage(playerId, message);
        }
        messages = null;
    }

    @Override
    public boolean isRunning() {

        return count < 9 && winner == null;
    }

    @Override
    public void finalize(GameMovieBuilder movieBuilder) {

        if (messages == null) {
            messages = new HashMap<>();

            for (String player : playersPieces.keySet()) {
                messages.put(player, new ArrayList<>());
            }
        }

        movieBuilder.addFrame();

        buildFrame(movieBuilder);

        if (winner == null) {
            for (String player : playersPieces.keySet()) {
                playersPieces.get(player).observations = "It's a tie!";
                playersPieces.get(player).points = DRAW_POINTS;

                messages.get(player).add("It's a tie!");
            }
        }

        for (String player : playersPieces.keySet()) {
            movieBuilder.setObservations(player, playersPieces.get(player).observations);
            movieBuilder.setPoints(player, playersPieces.get(player).points);
            movieBuilder.setClassification(player, playersPieces.get(player).classification);
        }
    }

    String getPlayerX() {
        return playerX;
    }

    String getPlayerO() {
        return playerO;
    }

    /**
     * Check if move is valid (i.e. position is not occupied)
     *
     * @param move position of the board to play
     * @return boolean <code>true</code> if move is valid, <code>false</code>
     * otherwise
     */
    private boolean valid(int move) {
        return board[move] == ' ';
    }

    /**
     * Check if player with piece c has won
     *
     * @param c piece
     * @return boolean <code>true</code> if c has won, <code>false</code>
     * otherwise
     */
    private boolean wins(char c) {
        return (board[1] == c && board[2] == c && board[3] == c) || (board[4] == c && board[5] == c && board[6] == c)
                || (board[7] == c && board[8] == c && board[9] == c)
                || (board[1] == c && board[4] == c && board[7] == c)
                || (board[2] == c && board[5] == c && board[8] == c)
                || (board[3] == c && board[6] == c && board[9] == c)
                || (board[1] == c && board[5] == c && board[9] == c)
                || (board[7] == c && board[5] == c && board[3] == c);
    }
}
