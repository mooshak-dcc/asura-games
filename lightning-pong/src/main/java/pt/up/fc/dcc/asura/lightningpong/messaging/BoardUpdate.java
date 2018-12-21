package pt.up.fc.dcc.asura.lightningpong.messaging;

import pt.up.fc.dcc.asura.lightningpong.models.Bonus;

import java.util.ArrayList;
import java.util.List;

/**
 * Update about the state of each object in the board
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class BoardUpdate {

    private Position ball;
    private MeUpdate me;
    private OpponentUpdate opponent;

    private List<PowerUpUpdate> powerUps = new ArrayList<>();

    private List<Position> lasers = new ArrayList<>();

    private ScoreUpdate score;

    public BoardUpdate() {
    }

    public Position getBall() {
        return ball;
    }

    public void setBall(Position ball) {
        this.ball = ball;
    }

    public MeUpdate getMe() {
        return me;
    }

    public void setMe(MeUpdate me) {
        this.me = me;
    }

    public OpponentUpdate getOpponent() {
        return opponent;
    }

    public void setOpponent(OpponentUpdate opponent) {
        this.opponent = opponent;
    }

    public List<PowerUpUpdate> getPowerUps() {
        return powerUps;
    }

    public void addPowerUp(PowerUpUpdate powerUp) {
        this.powerUps.add(powerUp);
    }

    public List<Position> getLasers() {
        return lasers;
    }

    public void addLaser(Position laser) {
        this.lasers.add(laser);
    }

    public ScoreUpdate getScore() {
        return score;
    }

    public void setScore(ScoreUpdate score) {
        this.score = score;
    }
}
