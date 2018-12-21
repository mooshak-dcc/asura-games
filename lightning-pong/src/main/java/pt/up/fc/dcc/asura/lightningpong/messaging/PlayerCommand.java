package pt.up.fc.dcc.asura.lightningpong.messaging;

import pt.up.fc.dcc.asura.lightningpong.models.Direction;

import java.util.List;

/**
 * Command sent from the Pong player
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class PlayerCommand {

    private int vertical;
    private int horizontal;
    private boolean fire;

    private List<String> messages;

    public PlayerCommand() {
    }

    public PlayerCommand(int vertical, int horizontal, boolean fire, List<String> messages) {
        this.vertical = vertical;
        this.horizontal = horizontal;
        this.fire = fire;
        this.messages = messages;
    }

    public int getVertical() {
        return vertical;
    }

    public void setVertical(int vertical) {
        this.vertical = vertical;
    }

    public int getHorizontal() {
        return horizontal;
    }

    public void setHorizontal(int horizontal) {
        this.horizontal = horizontal;
    }

    public boolean isFire() {
        return fire;
    }

    public void setFire(boolean fire) {
        this.fire = fire;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
