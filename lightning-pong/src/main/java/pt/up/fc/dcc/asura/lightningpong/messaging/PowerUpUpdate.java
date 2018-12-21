package pt.up.fc.dcc.asura.lightningpong.messaging;

import pt.up.fc.dcc.asura.lightningpong.models.Bonus;

/**
 * Update about a power up
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class PowerUpUpdate extends Position {

    private Bonus type;

    public PowerUpUpdate(int x, int y, Bonus type) {
        super(x, y);
        this.type = type;
    }

    public Bonus getType() {
        return type;
    }

    public void setType(Bonus type) {
        this.type = type;
    }
}
