
import bullseye.wrappers.java.BullseyePlayer;

import java.util.Random;

/**
 * Bullseye player in Java
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class MyBullseyePlayer extends BullseyePlayer {

    Random random = null;
    int lastHorizAngle;
    int lastVertAngle;

    @Override
    public String getName() {
        return "José Carlos Paiva";
    }

    @Override
    public void init() {
        random = new Random();
    }

    @Override
    public void execute() {

        int[] lastHit = getLastHit();

        if (lastHit == null) {
            lastHorizAngle = (int) (random.nextDouble() * 30 - 30);
            lastVertAngle = (int) (random.nextDouble() * 30 - 30);
            shoot(lastHorizAngle, lastVertAngle);

            return;
        }

        if (lastHit[0] > 400) {
            lastHorizAngle = Math.max(lastHorizAngle - 10, -80);
        } else if (lastHit[0] < 200) {
            lastHorizAngle = Math.min(lastHorizAngle + 10, 80);
        } else
            lastHorizAngle = (int) Math.max(Math.min(lastHorizAngle + random.nextDouble() * 2.5, 80), -80);

        if (lastHit[1] > 400) {
            lastVertAngle = Math.max(lastVertAngle - 10, -80);
        } else if (lastHit[1] < 200) {
            lastVertAngle = Math.min(lastVertAngle + 10, 80);
        } else
            lastVertAngle = (int) Math.max(Math.min(lastVertAngle + random.nextDouble() * 2.5, 80), -80);

        shoot(lastHorizAngle, lastVertAngle);
    }
}
