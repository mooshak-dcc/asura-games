package pt.up.fc.dcc.asura.lightningpong.messaging;

/**
 * Update related to the self player's paddle
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class MeUpdate extends Position {

    private long larger;
    private long faster;
    private long frozen;

    public MeUpdate(int x, int y, long larger, long faster, long frozen) {
        super(x, y);
        this.larger = larger;
        this.faster = faster;
        this.frozen = frozen;
    }

    public long getLarger() {
        return larger;
    }

    public void setLarger(long larger) {
        this.larger = larger;
    }

    public long getFaster() {
        return faster;
    }

    public void setFaster(long faster) {
        this.faster = faster;
    }

    public long getFrozen() {
        return frozen;
    }

    public void setFrozen(long frozen) {
        this.frozen = frozen;
    }
}
