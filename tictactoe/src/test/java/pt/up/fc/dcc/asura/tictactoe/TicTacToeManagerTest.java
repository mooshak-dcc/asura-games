package pt.up.fc.dcc.asura.tictactoe;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the manager of the TicTacToe.
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class TicTacToeManagerTest {

    @Test
    public void minPlayersPerMatchTest() {
        TicTacToeManager manager = new TicTacToeManager();
        Assert.assertTrue("Minimum number of players should be greater or equal to 1",
            manager.getMinPlayersPerMatch() >= 1);
    }

    @Test
    public void maxPlayersPerMatchTest() {
        TicTacToeManager manager = new TicTacToeManager();
        Assert.assertTrue("Maximum number of players should be greater or equal to minimum number of players",
            manager.getMaxPlayersPerMatch() >= manager.getMinPlayersPerMatch());
    }
}
