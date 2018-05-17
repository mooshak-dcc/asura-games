package pt.up.fc.dcc.asura.bullseye;

import pt.up.fc.dcc.asura.builder.base.*;
import pt.up.fc.dcc.asura.builder.base.messaging.PlayerAction;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilderImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager of the Bullseye.
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class BullseyeManager extends GameManager {

    private GameState currentState = null;

    @Override
    public String getGameStateClassName() {
        return BullseyeState.class.getCanonicalName();
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

        if (players.size() < 1)
            throw new IllegalArgumentException("Invalid number of players: " + players.size());

        currentState = state;

        movieBuilder = new GameMovieBuilderImpl();

        try (Streamer streamer = new Streamer(players)) {

            // collect player names
            Map<String, String> playerNames = new HashMap<>();
            for (String player : players.keySet()) {
                String name = getName(streamer.readActionFrom(player));
                playerNames.put(player, name);
            }

            // prepare state
            currentState.prepare(movieBuilder, playerNames);

            // run game
            while (currentState.isRunning()) {
                String player = ((BullseyeState) currentState).getTurn();

                streamer.sendStateUpdateTo(player, currentState.getStateUpdateFor(player));

                PlayerAction action = streamer.readActionFrom(player);

                currentState.execute(movieBuilder, player, action);

                currentState.endRound(movieBuilder);
            }

            for (String player : players.keySet())
                streamer.sendStateUpdateTo(player, null);

            currentState.finalize(movieBuilder);
        }
    }
}
