package pt.up.fc.dcc.asura.snake.levels;

import pt.up.fc.dcc.asura.snake.models.Actor;
import pt.up.fc.dcc.asura.snake.models.Block;
import pt.up.fc.dcc.asura.snake.utils.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager of the levels
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
public class LevelManager {

    private static final int[][][] BLOCK_POSITIONS = new int[][][]{
            new int[][] {
            },
            new int[][] {
                    new int[] { 1, 1 }, new int[] { -1, 1 },
                    new int[] { 1, -1 }, new int[] { -1, -1 }
            },
            new int[][] {
                    new int[] { 1, 1 }, new int[] { 2, 1 }, new int[] { 1, 2 },
                    new int[] { -1, 1 }, new int[] { -2, 1 }, new int[] { -1, 2 },
                    new int[] { 1, -1 }, new int[] { 1, -2 }, new int[] { 2, -1 },
                    new int[] { -1, -1 }, new int[] { -1, -2 }, new int[] { -2, -1 }
            },
            new int[][] {
                    new int[] { 1, 1 }, new int[] { 3, 1 }, new int[] { 1, 3 },
                    new int[] { -1, 1 }, new int[] { -3, 1 }, new int[] { -1, 3 },
                    new int[] { 1, -1 }, new int[] { 1, -3 }, new int[] { 3, -1 },
                    new int[] { -1, -1 }, new int[] { -1, -3 }, new int[] { -3, -1 }
            },
            new int[][] {
                    new int[] { 1, 1 }, new int[] { 3, 1 }, new int[] { 1, 3 },
                    new int[] { -1, 1 }, new int[] { -3, 1 }, new int[] { -1, 3 },
                    new int[] { 1, -1 }, new int[] { 1, -3 }, new int[] { 3, -1 },
                    new int[] { -1, -1 }, new int[] { -1, -3 }, new int[] { -3, -1 },
                    new int[] { 1, 1 }, new int[] { 5, 1 }, new int[] { 1, 5 },
                    new int[] { -1, 1 }, new int[] { -5, 1 }, new int[] { -1, 5 },
                    new int[] { 1, -1 }, new int[] { 1, -5 }, new int[] { 5, -1 },
                    new int[] { -1, -1 }, new int[] { -1, -5 }, new int[] { -5, -1 },
                    new int[] { 1, 1 }, new int[] { 7, 1 }, new int[] { 1, 7 },
                    new int[] { -1, 1 }, new int[] { -7, 1 }, new int[] { -1, 7 },
                    new int[] { 1, -1 }, new int[] { 1, -7 }, new int[] { 7, -1 },
                    new int[] { -1, -1 }, new int[] { -1, -7 }, new int[] { -7, -1 }
            }
    };


    public static Level generateLevel(int width, int height, int tileLength, int levelNo) {

        int hTiles = height / tileLength;
        int vTiles = width / tileLength;

        Level level = new Level(hTiles, vTiles);

        levelNo = levelNo - 1;
        for (int i = 0; i < BLOCK_POSITIONS[levelNo].length; i++)
            level.addBlock(
                    BLOCK_POSITIONS[levelNo][i][0] > 0 ? BLOCK_POSITIONS[levelNo][i][0] : (hTiles - 1) + BLOCK_POSITIONS[levelNo][i][0],
                    BLOCK_POSITIONS[levelNo][i][1] > 0 ? BLOCK_POSITIONS[levelNo][i][1] : (vTiles - 1) + BLOCK_POSITIONS[levelNo][i][1]);

        return level;
    }
}
