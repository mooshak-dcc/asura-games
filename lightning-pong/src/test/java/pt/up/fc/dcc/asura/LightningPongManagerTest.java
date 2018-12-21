package pt.up.fc.dcc.asura;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the manager of the Lightning Pong.
 *
 * @author José C. Paiva <code>josepaiva94@gmail.com</code>
 */
public class LightningPongManagerTest {

    @Test
    public void minPlayersPerMatchTest() {
        LightningPongManager manager = new LightningPongManager();
        Assert.assertTrue("Minimum number of players should be greater or equal to 1",
            manager.getMinPlayersPerMatch() >= 1);
    }

    @Test
    public void maxPlayersPerMatchTest() {
        LightningPongManager manager = new LightningPongManager();
        Assert.assertTrue("Maximum number of players should be greater or equal to minimum number of players",
            manager.getMaxPlayersPerMatch() >= manager.getMinPlayersPerMatch());
    }
}
