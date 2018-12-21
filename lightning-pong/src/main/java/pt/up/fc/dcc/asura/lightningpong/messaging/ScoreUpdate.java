package pt.up.fc.dcc.asura.lightningpong.messaging;

/**
 * Update on the match score
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class ScoreUpdate {

    private int me;
    private int opponent;

    public ScoreUpdate(int me, int opponent) {
        this.me = me;
        this.opponent = opponent;
    }

    public int getMe() {
        return me;
    }

    public void setMe(int me) {
        this.me = me;
    }

    public int getOpponent() {
        return opponent;
    }

    public void setOpponent(int opponent) {
        this.opponent = opponent;
    }
}
