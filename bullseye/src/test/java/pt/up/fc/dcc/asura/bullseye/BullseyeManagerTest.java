package pt.up.fc.dcc.asura.bullseye;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the manager of the Bullseye.
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class BullseyeManagerTest {

    @Test
    public void minPlayersPerMatchTest() {
        BullseyeManager manager = new BullseyeManager();
        Assert.assertTrue("Minimum number of players should be greater or equal to 1",
            manager.getMinPlayersPerMatch() >= 1);
    }

    @Test
    public void maxPlayersPerMatchTest() {
        BullseyeManager manager = new BullseyeManager();
        Assert.assertTrue("Maximum number of players should be greater or equal to minimum number of players",
            manager.getMaxPlayersPerMatch() >= manager.getMinPlayersPerMatch());
    }
}
