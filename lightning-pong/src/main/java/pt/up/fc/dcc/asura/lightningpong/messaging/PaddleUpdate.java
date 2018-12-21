package pt.up.fc.dcc.asura.lightningpong.messaging;

/**
 * Updates on the paddle
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class PaddleUpdate {

    private boolean moved;
    private boolean fired;

    public PaddleUpdate(boolean moved, boolean fired) {
        this.moved = moved;
        this.fired = fired;
    }


}
