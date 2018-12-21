package pt.up.fc.dcc.asura.lightningpong.messaging;

import pt.up.fc.dcc.asura.lightningpong.models.Bonus;

import java.util.ArrayList;
import java.util.List;

/**
 * Update object sent from the Pong manager to the players
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class PongUpdate {

    private BoardUpdate board = new BoardUpdate();
    private CommandResult result;

    public PongUpdate() {
    }

    public void setBall(int x, int y) {
        this.board.setBall(new Position(x, y));
    }

    public void setMe(int x, int y, long larger, long faster, long frozen) {
        this.board.setMe(new MeUpdate(x, y, larger, faster, frozen));
    }

    public void setOpponent(int x, int y, boolean larger, boolean faster, boolean frozen) {
        this.board.setOpponent(new OpponentUpdate(x, y, larger, faster, frozen));
    }

    public void addPowerUp(int x, int y, Bonus bonus) {
        this.board.addPowerUp(new PowerUpUpdate(x, y, bonus));
    }

    public void addLaser(int x, int y) {
        this.board.addLaser(new Position(x, y));
    }

    public void setScore(int me, int opponent) {
        this.board.setScore(new ScoreUpdate(me, opponent));
    }

    public BoardUpdate getBoard() {
        return board;
    }

    public void setResult(CommandResult result) {
        this.result = result;
    }

    public CommandResult getResult() {
        return result;
    }
}
