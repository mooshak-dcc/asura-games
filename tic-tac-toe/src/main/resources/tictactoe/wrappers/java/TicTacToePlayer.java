package tictactoe.wrappers.java;

import wrappers.java.StateUpdate;
import wrappers.java.PlayerWrapper;

/**
 * Abstract class to be extended by concrete Tic Tac Toe players
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public abstract class TicTacToePlayer extends PlayerWrapper {

    private int[] lastPlayed = null;

    @Override
    public final void update(StateUpdate update) {
        int position = update.getObjectAsInt();
        if (position == 0) return;
        lastPlayed = new int[] { (position - 1) % 3, (position - 1) / 3 };
    }

    @Override
    public final void run() {

        // game flow
        while (true) {
            readAndUpdate();
            execute();
            sendAction();
        }
    }

    // ----------------- Provide any additional functions below ---------------

    public int[] getLastPlayed() {
        return lastPlayed;
    }

    public void play(int x, int y) {
        doAction("PLAY", y * 3 + x + 1);
    }
}
