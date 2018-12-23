package pt.up.fc.dcc.asura.snake;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the manager of the Snake.
 *
 * @author José C. Paiva <code>josepaiva94@gmail.com</code>
 */
public class SnakeManagerTest {

    @Test
    public void minPlayersPerMatchTest() {
        SnakeManager manager = new SnakeManager();
        Assert.assertTrue("Minimum number of players should be greater or equal to 1",
            manager.getMinPlayersPerMatch() >= 1);
    }

    @Test
    public void maxPlayersPerMatchTest() {
        SnakeManager manager = new SnakeManager();
        Assert.assertTrue("Maximum number of players should be greater or equal to minimum number of players",
            manager.getMaxPlayersPerMatch() >= manager.getMinPlayersPerMatch());
    }
}
