package pt.up.fc.dcc.asura;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the manager of the Asteroids.
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class AsteroidsManagerTest {

    @Test
    public void minPlayersPerMatchTest() {
        AsteroidsManager manager = new AsteroidsManager();
        Assert.assertTrue("Minimum number of players should be greater or equal to 1",
            manager.getMinPlayersPerMatch() >= 1);
    }

    @Test
    public void maxPlayersPerMatchTest() {
        AsteroidsManager manager = new AsteroidsManager();
        Assert.assertTrue("Maximum number of players should be greater or equal to minimum number of players",
            manager.getMaxPlayersPerMatch() >= manager.getMinPlayersPerMatch());
    }
}
