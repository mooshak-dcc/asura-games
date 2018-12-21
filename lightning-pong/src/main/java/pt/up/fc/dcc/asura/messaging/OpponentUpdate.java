package pt.up.fc.dcc.asura.messaging;

/**
 * Update related to the opponent player's paddle
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class OpponentUpdate extends Position {

    private boolean larger;
    private boolean faster;
    private boolean frozen;

    public OpponentUpdate(int x, int y, boolean larger, boolean faster, boolean frozen) {
        super(x, y);
        this.larger = larger;
        this.faster = faster;
        this.frozen = frozen;
    }

    public boolean isLarger() {
        return larger;
    }

    public void setLarger(boolean larger) {
        this.larger = larger;
    }

    public boolean isFaster() {
        return faster;
    }

    public void setFaster(boolean faster) {
        this.faster = faster;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }
}
