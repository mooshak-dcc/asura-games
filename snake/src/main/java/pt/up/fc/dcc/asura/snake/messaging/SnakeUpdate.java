package pt.up.fc.dcc.asura.snake.messaging;

import pt.up.fc.dcc.asura.snake.models.Direction;
import pt.up.fc.dcc.asura.snake.utils.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Update object sent to the player
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class SnakeUpdate {

    private List<Vector> snake = new ArrayList<>();
    private Direction direction;
    private List<Vector> newFruits = new ArrayList<>();
    private List<Vector> expiredFruits = new ArrayList<>();

    public SnakeUpdate(Direction direction) {
        this.direction = direction;
    }

    public List<Vector> getSnake() {
        return snake;
    }

    public void addSnakeSection(Vector section) {
        snake.add(section);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public List<Vector> getNewFruits() {
        return newFruits;
    }

    public void addNewFruit(Vector newFruit) {
        newFruits.add(newFruit);
    }

    public List<Vector> getExpiredFruits() {
        return expiredFruits;
    }

    public void addExpiredFruit(Vector expiredFruit) {
        expiredFruits.add(expiredFruit);
    }
}
