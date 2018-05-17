package bullseye.wrappers.java;

import wrappers.java.StateUpdate;
import wrappers.java.PlayerWrapper;

/**
 * Abstract class to be extended by concrete Bullseye players
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public abstract class BullseyePlayer extends PlayerWrapper {

    private Integer bullseyeX = null, bullseyeY = null;
    private String playerId;

    @Override
    public final void update(StateUpdate update) {

        if (update == null || update.getType() == null || update.getObject() == null)
            return;

        if ("HIT".equals(update.getType())) {

            String[] pos = ((String) update.getObject()).split(" ");
            bullseyeX = Integer.parseInt(pos[0]);
            bullseyeY = Integer.parseInt(pos[1]);
        }
    }

    @Override
    public final void run() {

        // game flow
        while (true) {
            readAndUpdate();

            execute();

            sendAction();
        }
    }

    /**
     * Play in position pos of the board
     *
     * @param horizontalAngle Horizontal angle of the shot
     * @param verticalAngle Vertical angle of the shot
     */
    protected void shoot(int horizontalAngle, int verticalAngle) {
        doAction("SHOOT", horizontalAngle, verticalAngle);
    }

    /**
     * Last position of the hit
     * @return last position of the hit
     */
    protected int[] getLastHit() {
        if (bullseyeX == null || bullseyeY == null)
            return null;
        return new int[] { bullseyeX, bullseyeY };
    }
}
