package pt.up.fc.dcc.asura.bullseye;

import pt.up.fc.dcc.asura.builder.base.*;
import pt.up.fc.dcc.asura.builder.base.exceptions.BuilderException;
import pt.up.fc.dcc.asura.builder.base.exceptions.PlayerException;
import pt.up.fc.dcc.asura.builder.base.messaging.PlayerAction;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager of the Bullseye.
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class BullseyeManager extends GameManager {

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
            state.prepare(movieBuilder, playerNames);

            // run game
            while (state.isRunning()) {
                String player = ((BullseyeState) state).getTurn();

                streamer.sendStateUpdateTo(player, state.getStateUpdateFor(player));

                PlayerAction action = streamer.readActionFrom(player);

                state.execute(movieBuilder, player, action);

                state.endRound(movieBuilder);
            }

            for (String player : players.keySet())
                streamer.sendStateUpdateTo(player, null);


            // finalize state
            state.finalize(movieBuilder);

        } catch (IOException e) {
            throw new BuilderException(e.getMessage());
        }
    }

}
