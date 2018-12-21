package pt.up.fc.dcc.asura.models;

/**
 * Representation of a Pong player
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class Player {

    private String playerId;
    private String playerName;

    private Paddle paddle;

    private int score = 0;

    public Player(String playerId, String playerName, Paddle paddle) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.paddle = paddle;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public int getScore() {
        return score;
    }

    public void addPoint() {
        score++;
    }

    public void resetScore() {
        score = 0;
    }
}
