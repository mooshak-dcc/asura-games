package pt.up.fc.dcc.asura.snake;

import pt.up.fc.dcc.asura.builder.base.exceptions.PlayerException;
import pt.up.fc.dcc.asura.builder.base.messaging.Command;
import pt.up.fc.dcc.asura.builder.base.messaging.PlayerAction;
import pt.up.fc.dcc.asura.builder.base.messaging.StateUpdate;
import pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder;
import pt.up.fc.dcc.asura.builder.base.GameState;
import pt.up.fc.dcc.asura.builder.base.movie.models.MooshakClassification;
import pt.up.fc.dcc.asura.snake.levels.Level;
import pt.up.fc.dcc.asura.snake.levels.LevelManager;
import pt.up.fc.dcc.asura.snake.messaging.BoardUpdate;
import pt.up.fc.dcc.asura.snake.messaging.SnakeUpdate;
import pt.up.fc.dcc.asura.snake.models.*;
import pt.up.fc.dcc.asura.snake.utils.SpriteManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static pt.up.fc.dcc.asura.builder.base.movie.GameMovieBuilder.SpriteAnchor;


/**
 * Stores the current state of the Snake game
 *
 * @author José C. Paiva <code>josepaiva94@gmail.com</code>
 */
public class SnakeState implements GameState {
    public static final int GAME_WORLD_WIDTH = 600;
    public static final int GAME_WORLD_HEIGHT = 600;

    private static final int FPS = 5;

    public static final int TILE_LENGTH = 40;

    private static final int MAX_TIME = 10000;
    private static final int MAX_LEVEL = 5;

    private static final int GENERATE_FOOD_TIME = 40;
    private static final int FOOD_EXPIRY_TIME = 60;
    private static final int MAX_FRUITS_PER_LEVEL = 16;
    private static final int MAX_EXPIRED_FRUITS = 2;

    private static final String BACKGROUND_SPRITE = "bg.png";

    private static final String MSG_LOSER_BLOCK = "This was a pebble, %s! The snake " +
            "will surely have a bad experience after eating this.";
    private static final String MSG_LOSER_TAIL = "The snake ate its own tail, %s! It hurt " +
            "only by watching this.";
    private static final String MSG_LOSER_BOUNDS = "The snake escaped, %s! We love the snake, " +
            "but she is part of our experiment. Please, don't let her crawl out!";
    private static final String MSG_LOSER_EXPIRED_FRUIT = "This is a waste, %s!" +
            " You're leaving the good fruit for humans, and the snake will eat " +
            "what?! Fast food?!";
    private static final String MSG_WINNER = "Nice work, %s! The snake will stay healthy " +
            "and die of old age, after learning to eat fruit with you!";

    private String playerId;
    private String playerName;

    private long time = 0;
    private int score = 0;

    private int currentLevel = 1;
    private boolean newLevel = true;
    private Level level;

    private Snake snake;
    private List<Fruit> fruits = new ArrayList<>();

    private int fruitIndex = 0;

    private Fruit newFruit = null;
    private long newFruitTime = -1;
    private List<Fruit> expiredFruits = new ArrayList<>();

    private int expiredFruitCount = 0;

    private boolean alive = true;
    private String endMessage = null;

    private Direction lastCommand;
    private List<String> messages = null;

    @Override
    public void prepare(GameMovieBuilder builder, String title, Map<String, String> players) {

        builder.setTitle(title);
        builder.setWidth(GAME_WORLD_WIDTH);
        builder.setHeight(GAME_WORLD_HEIGHT);
        builder.setBackground(BACKGROUND_SPRITE);
        builder.setFps(FPS);
        builder.setSpriteAnchor(SpriteAnchor.TOP_LEFT);

        playerId = players.keySet().iterator().next();
        playerName = players.get(playerId);

        // add players
        builder.addPlayer(playerId, playerName);

        // load sprites
        SpriteManager.load(builder);

        // set up
        setupLevel();
    }

    private void setupLevel() {

        if (currentLevel > MAX_LEVEL)
            return;

        time = 0;
        snake = new Snake();
        level = LevelManager.generateLevel(GAME_WORLD_WIDTH, GAME_WORLD_HEIGHT,
                TILE_LENGTH, currentLevel);

        newLevel = true;
        level.placeSnake(snake);
        snake.buildTail();

        newFruitTime = 0;
        newFruit = null;
        fruits.clear();
        expiredFruits.clear();
        expiredFruitCount = 0;
        fruitIndex = 0;
    }

    @Override
    public void execute(GameMovieBuilder builder, String playerId, PlayerAction action) {

        Command command = action.getCommand();

        if (command != null) {

            try {
                lastCommand = Direction.valueOf(command.getName());
                messages = action.getMessages();
            } catch (IllegalArgumentException e) {
                builder.addFrame();
                builder.wrongAnswer(playerId, e.getMessage());
                throw e;
            };
        }
    }

    @Override
    public StateUpdate getStateUpdateFor(String player) {

        if (player == null) {

            BoardUpdate updateObj = new BoardUpdate(level.getWidth(), level.getHeight());

            for (Block block: level.getBlocks())
                updateObj.addBlock(block.getPosition());

            updateObj.setNewLevel(newLevel);
            newLevel = false;

            return new StateUpdate("BOARD", updateObj);
        } else {

            SnakeUpdate updateObj = new SnakeUpdate(snake.getDirection());

            updateObj.addSnakeSection(snake.getPosition());

            for (TailSection section: snake.tail())
                updateObj.addSnakeSection(section.getPosition());

            if (newFruit != null) {
                updateObj.addNewFruit(newFruit.getPosition());
                newFruit = null;
            }

            for (Fruit expiredFruit: expiredFruits)
                updateObj.addExpiredFruit(expiredFruit.getPosition());
            expiredFruits.clear();

            return new StateUpdate("SNAKE", updateObj);
        }
    }

    @Override
    public void endRound(GameMovieBuilder builder) {

        // process command
        processCommand(builder);

        // update actors
        snake.onUpdate();

        // check bounds
        if (level.isSnakeOutOfBounds()) {
            alive = false;
            endMessage = MSG_LOSER_BOUNDS;
        }

        // check fruit collisions
        List<Fruit> oldFruits = new ArrayList<>(fruits);
        for (Fruit fruit: oldFruits) {

            if (time - fruit.getTime() > FOOD_EXPIRY_TIME) {
                fruits.remove(fruit);
                expiredFruits.add(fruit);
                expiredFruitCount++;

                if (expiredFruitCount > MAX_EXPIRED_FRUITS) {
                    alive = false;
                    endMessage = MSG_LOSER_EXPIRED_FRUIT;
                }
            } else if (level.collides(snake, fruit)) {
                snake.eat();
                score++;

                fruits.remove(fruit);
                expiredFruits.add(fruit);
            }
        }

        // check block collisions
        if (level.isSnakeCrashedInBlocks()) {
            alive = false;
            endMessage = MSG_LOSER_BLOCK;
        }

        // check self collision
        if (snake.eatsTail()) {
            alive = false;
            endMessage = MSG_LOSER_TAIL;
        }

        // generate fruit?
        if (fruitIndex < MAX_FRUITS_PER_LEVEL &&
                ((time - newFruitTime) % GENERATE_FOOD_TIME == 0 || (fruits.isEmpty() && Math.random() < 0.1)))
            generateNewFruit();

        // draw the frame
        buildFrame(builder);

        if (fruitIndex >= MAX_FRUITS_PER_LEVEL && fruits.isEmpty() && alive) {
            currentLevel++;
            setupLevel();
        }

        time++;
    }

    @Override
    public boolean isRunning() {
        return alive && time < MAX_TIME && currentLevel <= MAX_LEVEL;
    }

    @Override
    public void finalize(GameMovieBuilder builder) {

        builder.saveFrame();

        builder.addFrame();

        builder.restoreFrame();

        if (alive) {
            builder.setObservations(playerId, String.format(MSG_WINNER, playerName));
            builder.addMessage(playerId, String.format(MSG_WINNER, playerName));
            builder.setClassification(playerId, MooshakClassification.ACCEPTED);
            builder.setPoints(playerId, score);
        } else {
            builder.setObservations(playerId, String.format(endMessage, playerName));
            builder.addMessage(playerId, String.format(endMessage, playerName));
            builder.setClassification(playerId, MooshakClassification.WRONG_ANSWER);
            builder.setPoints(playerId, score);
        }
    }

    private void processCommand(GameMovieBuilder builder) {
        if (lastCommand != Direction.IDLE) {

            boolean move;
            switch (lastCommand) {

                case UP:
                    move = snake.up();
                    break;
                case RIGHT:
                    move = snake.right();
                    break;
                case DOWN:
                    move = snake.down();
                    break;
                case LEFT:
                default:
                    move = snake.left();
                    break;
            }

            if (!move) {
                builder.addFrame();
                throw new PlayerException(playerId, MooshakClassification.WRONG_ANSWER,
                        "You've tried to change the snake direction to the complete opposite direction!");
            }
        }
    }

    private void buildFrame(GameMovieBuilder builder) {

        builder.addFrame();

        if (messages != null)
            for (String message: messages)
                builder.addMessage(playerId, message);

        for (Block block: level.getBlocks())
            block.draw(builder);

        snake.draw(builder);

        for (Fruit fruit: fruits)
            fruit.draw(builder);
    }

    private void generateNewFruit() {
        Fruit fruit = new Fruit(fruitIndex++, time);
        level.placeFruit(fruit);
        fruits.add(fruit);

        newFruit = fruit;
        newFruitTime = time;
    }
}
