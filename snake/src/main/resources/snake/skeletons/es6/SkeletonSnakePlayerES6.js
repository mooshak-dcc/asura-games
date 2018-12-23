// ------------------------ Skeleton --------------------------------

load('snake/wrappers/es6/SnakePlayer.js');

(function (exports) {'use strict';

    /**
     * Snake player in Javascript
     *
     * @author José C. Paiva <code>josepaiva94@gmail.com</code>
     */
    class MySnakePlayer extends SnakePlayer {

        // ---------------------- Your code below ---------------------------

        /**
         * Get the name of the player
         *
         * @returns {string} name of the player
         */
        getName() {
            return 'Your name';
        }

        /**
         * Initialize the player
         */
        init() {

            // tip: install handler for new / expired fruits

            // tip: define a class field for keeping current fruits
        }

        /**
         * Execute player action
         */
        execute() {

            // tip: get the puzzle and go towards a target fruit (if it exists),
            // avoiding barriers and snake tail
        }
    }


    // ------------------------ Skeleton --------------------------------

    exports.Player = MySnakePlayer;
}(this));
