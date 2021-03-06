package pt.up.fc.dcc.asura;

import pt.up.fc.dcc.asura.builder.base.*;
import pt.up.fc.dcc.asura.builder.base.exceptions.BuilderException;
import pt.up.fc.dcc.asura.builder.base.exceptions.PlayerException;
import pt.up.fc.dcc.asura.builder.base.messaging.PlayerAction;
import pt.up.fc.dcc.asura.builder.base.movie.models.MooshakClassification;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Manager of the Asteroids.
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class AsteroidsManager extends GameManager {
    private static final long ACTION_TIMEOUT_MS = 1000;

    @Override
    public String getGameStateClassName() {
        return AsteroidsState.class.getCanonicalName();
    }

    @Override
    public int getMaxPlayersPerMatch() {
        return 4;
    }

    @Override
    public int getMinPlayersPerMatch() {
        return 2;
    }

    @Override
    protected void manage(GameState state, Map<String, Process> players)
            throws BuilderException, PlayerException {

        if (players.size() < getMinPlayersPerMatch() || players.size() > getMaxPlayersPerMatch())
            throw new BuilderException("Invalid number of players: " + players.size());

        try (Streamer streamer = new Streamer(players)) {

            // collect player names
            Map<String, String> playerNames = new HashMap<>();
            for (String player : players.keySet()) {
                String name = getName(streamer.readActionFrom(player));
                playerNames.put(player, name);
            }

            // prepare state
            state.prepare(movieBuilder, getGameName(), playerNames);

            // send initial update to players
            for (String playerId: players.keySet()) {
                streamer.sendStateUpdateTo(playerId, state.getStateUpdateFor(playerId));
            }

            // run game
            while (state.isRunning()) {

                for (String playerId: players.keySet()) {
                    streamer.sendStateUpdateTo(playerId, state.getStateUpdateFor(playerId));

                    PlayerAction action = streamer.readActionWithTimeoutFrom(playerId, ACTION_TIMEOUT_MS);
                    state.execute(movieBuilder, playerId, action);
                }

                // players execute actions at the same time
                state.endRound(movieBuilder);
            }

            // finalize state
            state.finalize(movieBuilder);

        } catch (IOException e) {
            throw new BuilderException(e.getMessage());
        }
    }
}
