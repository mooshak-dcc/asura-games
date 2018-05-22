
// ------------------------ Skeleton --------------------------------

load('tictactoe/wrappers/js/TicTacToePlayer.js');

(function (exports) {'use strict';

    /**
     * TicTacToe player in Javascript
     *
     * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
     */
    function MyTicTacToePlayer() {
        TicTacToePlayer.call(this);
    }

    MyTicTacToePlayer.prototype = Object.create(TicTacToePlayer.prototype);

    // ---------------------- Your code below ---------------------------

    /**
     * Get the name of the player
     *
     * @returns {string} name of the player
     */
    MyTicTacToePlayer.prototype.getName = function () {
        return 'Professor X';
    };

    /**
     * Initialize the player
     */
    MyTicTacToePlayer.prototype.init = function () {
    };

    /**
     * Execute player action
     */
    MyTicTacToePlayer.prototype.execute = function () {
        this.play(this.generateMove());
    };

    /**
     * Generates randomly a valid move
     *
     * @returns {number} a valid move
     */
    MyTicTacToePlayer.prototype.generateMove = function () {

        var possibleMoves = [];

        var i = 1;
        while (i <= 9) {
            if (this.isFree(i))
                possibleMoves.push(i);
            i++;
        }

        if (possibleMoves.length === 0)
            return 0;

        return possibleMoves[getRandomInt(possibleMoves.length)];
    };

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

