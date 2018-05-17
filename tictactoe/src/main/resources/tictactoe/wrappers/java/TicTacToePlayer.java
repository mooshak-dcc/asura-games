package tictactoe.wrappers.java;

import wrappers.java.StateUpdate;
import wrappers.java.PlayerWrapper;

import java.util.List;

/**
 * Abstract class to be extended by concrete TicTacToe players
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public abstract class TicTacToePlayer extends PlayerWrapper {

    private char[] board;
    private char piece;

    @SuppressWarnings("unchecked")
    @Override
    public final void update(StateUpdate update) {

        switch (update.getType()) {

            case "PLAYER_X":
                if (getPlayerId().equals(update.getObject()))
                    piece = 'X';
                break;
            case "PLAYER_O":
                if (getPlayerId().equals(update.getObject()))
                    piece = 'O';
                break;
            case "BOARD":
                board = new char[10];
                int i = 0;

                List<String> boardList = (List<String>) (update.getObject());
                for (String el: boardList) {
                    board[i++] = el.charAt(0);
                }
                break;
        }
    }

    @Override
    public final void run() {

        // read X player
        readAndUpdate();

        //read O player
        readAndUpdate();

        // read initial state
        readAndUpdate();

        // game flow
        while (true) {
            readAndUpdate();

            execute();

            sendAction();
        }
    }

    /**
     * Play in position pos of the board
     *
     * @param pos
     *            Position of the board (between 1 and 9, counting from top-left
     *            to bottom-right)
     */
    protected void play(int pos) {
        doAction("PLAY", pos);
    }

    /**
     * Check if position is free
     *
     * @param pos
     *            Position of the board (between 1 and 9, counting from top-left
     *            to bottom-right)
     * @return boolean <code>true</code> if position is free, <code>false</code>
     *         otherwise
     */
    protected boolean isFree(int pos) {
        return board[pos] == ' ';
    }

    /**
     * Check if position is occupied by opponent
     *
     * @param pos
     *            Position of the board (between 1 and 9, counting from top-left
     *            to bottom-right)
     * @return boolean <code>true</code> if position is occupied by opponent,
     *         <code>false</code> otherwise
     */
    protected boolean isOpponent(int pos) {

        return board[pos] != piece && !isFree(pos);
    }

    /**
     * Check if position is occupied by self
     *
     * @param pos
     *            Position of the board (between 1 and 9, counting from top-left
     *            to bottom-right)
     * @return boolean <code>true</code> if position is occupied by self,
     *         <code>false</code> otherwise
     */
    protected boolean isSelf(int pos) {
        return board[pos] == piece;
    }
}
