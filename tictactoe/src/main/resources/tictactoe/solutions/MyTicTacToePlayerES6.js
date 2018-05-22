
// ------------------------ Skeleton --------------------------------

load('tictactoe/wrappers/es6/TicTacToePlayer.js');

(function (exports) {'use strict';

    /**
     * TicTacToe player in Javascript
     *
     * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
     */
    class MyTicTacToePlayer extends TicTacToePlayer {

        // ---------------------- Your code below ---------------------------

        /**
         * Get the name of the player
         *
         * @returns {string} name of the player
         */
        getName() {
            return 'Professor X';
        }

        /**
         * Initialize the player
         */
        init() {
        }

        /**
         * Execute player action
         */
        execute() {
            this.play(this.generateMove());
        }

        /**
         * Generates randomly a valid move
         *
         * @returns {number} a valid move
         */
        generateMove() {

            let possibleMoves = [];

            let i = 1;
            while (i <= 9) {
                if (this.isFree(i))
                    possibleMoves.push(i);
                i++;
            }

            if (possibleMoves.length === 0)
                return 0;

            return possibleMoves[getRandomInt(possibleMoves.length)];
        }
    }

    /**
     * Get a random int between 0 and max
     *
     * @param max {number} upper limit
     * @returns {number} random int between 0 and max
     */
    function getRandomInt(max) {
        return Math.floor(Math.random() * Math.floor(max));
    }

    // ------------------------ Skeleton --------------------------------

    exports.Player = MyTicTacToePlayer;
}(this));
