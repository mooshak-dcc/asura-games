import tictactoe.wrappers.java.TicTacToePlayer;

import java.util.Random;

/**
 * Tic Tac Toe player in Java
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class MyTicTacToePlayer extends TicTacToePlayer {

    private int[][] board;

    @Override
    public String getName() {
        return "José Carlos Paiva";
    }

    @Override
    public void init() {
        board = new int[3][3]; // 0 empty, 1 me, 2 opp
    }

    @Override
    public void execute() {
        if (getLastPlayed() != null)
            board[getLastPlayed()[1]][getLastPlayed()[0]] = 2;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0) {
                    play(j, i);
                    board[i][j] = 1;
                    return;
                }
            }
        }
    }
}
