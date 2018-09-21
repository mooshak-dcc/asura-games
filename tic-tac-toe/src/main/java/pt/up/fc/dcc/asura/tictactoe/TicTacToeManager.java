package pt.up.fc.dcc.asura.tictactoe;

import pt.up.fc.dcc.asura.builder.base.*;
import pt.up.fc.dcc.asura.builder.base.exceptions.BuilderException;
import pt.up.fc.dcc.asura.builder.base.exceptions.PlayerException;
import pt.up.fc.dcc.asura.builder.base.messaging.Command;
import pt.up.fc.dcc.asura.builder.base.messaging.PlayerAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager of the Tic Tac Toe.
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class TicTacToeManager extends GameManager {
    
    @Override
    public String getGameName() {
        return "Tic Tac Toe";
    }

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

            // TODO: run game

            // flip coin
            List<String> playerIds = new ArrayList<>(players.keySet());

            int crosses = (int) Math.round(Math.random());

            PlayerAction xSymbolAction = new PlayerAction();
            xSymbolAction.setCommand(new Command("SYMBOL", "X"));
            state.execute(movieBuilder, playerIds.get(crosses), xSymbolAction);

            PlayerAction oSymbolAction = new PlayerAction();
            oSymbolAction.setCommand(new Command("SYMBOL", "O"));
            state.execute(movieBuilder, playerIds.get((crosses + 1) % 2), oSymbolAction);

            int turn = crosses;
            while (state.isRunning()) {
                String player = playerIds.get(turn);

                streamer.sendStateUpdateTo(player, state.getStateUpdateFor(player));

                state.execute(movieBuilder, player, streamer.readActionFrom(player));

                state.endRound(movieBuilder);

                turn = (turn + 1) % 2;
            }

            // END TODO

            // finalize state
            state.finalize(movieBuilder);

        } catch (IOException e) {
            throw new BuilderException(e.getMessage());
        }
    }

}
