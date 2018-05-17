package pt.up.fc.dcc.asura.tictactoe;

import pt.up.fc.dcc.asura.builder.base.*;
import pt.up.fc.dcc.asura.builder.base.messaging.PlayerAction;
import pt.up.fc.dcc.asura.builder.base.messaging.StateUpdate;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilderImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager of the TicTacToe.
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class TicTacToeManager extends GameManager {

    @Override
    public String getGameStateClassName() {
        return TicTacToeState.class.getCanonicalName();
    }

    @Override
    public int getMaxPlayersPerMatch() {
        return 2;
    }

    @Override
    public int getMinPlayersPerMatch() {
        return 2;
    }

    @Override
    public void manage(GameState state, Map<String, Process> players) throws IOException {

        if (players.size() != 2)
            throw new IllegalArgumentException("Invalid number of players pieces: " + players.size());

        movieBuilder = new GameMovieBuilderImpl();

        try (Streamer streamer = new Streamer(players)) {

            // collect player names
            Map<String, String> playerNames = new HashMap<>();
            for (String player : players.keySet()) {
                PlayerAction action = streamer.readActionFrom(player);
                String name = getName(action);
                playerNames.put(player, name);
            }

            // prepare state
            state.prepare(movieBuilder, playerNames);

            // send player symbols to each player
            for (String player : players.keySet()) {
                streamer.sendStateUpdateTo(player, new StateUpdate("PLAYER_X",
                        ((TicTacToeState) state).getPlayerX()));
                streamer.sendStateUpdateTo(player, new StateUpdate("PLAYER_O",
                        ((TicTacToeState) state).getPlayerO()));
            }

            // send initial board state to every player
            for (String player : players.keySet())
                streamer.sendStateUpdateTo(player, state.getStateUpdateFor(player));

            boolean isX = true;
            while (state.isRunning()) {
                String player = isX ? ((TicTacToeState) state).getPlayerX() :
                        ((TicTacToeState) state).getPlayerO();

                streamer.sendStateUpdateTo(player, state.getStateUpdateFor(player));
                PlayerAction action = streamer.readActionFrom(player);

                state.execute(movieBuilder, player, action);

                state.endRound(movieBuilder);

                isX = !isX;
            }

            for (String player : players.keySet())
                streamer.sendStateUpdateTo(player, state.getStateUpdateFor(player));
        }

        state.finalize(movieBuilder);
    }

}
