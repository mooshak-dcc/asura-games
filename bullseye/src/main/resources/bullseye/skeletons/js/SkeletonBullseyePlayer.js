
// ------------------------ Skeleton --------------------------------

load('bullseye/wrappers/js/BullseyePlayer.js');

(function (exports) {'use strict';

    /**
     * Bullseye player in Javascript
     *
     * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
     */
    function MyBullseyePlayer() {
        BullseyePlayer.call(this);
    }

    MyBullseyePlayer.prototype = Object.create(BullseyePlayer.prototype);

    // ---------------------- Your code below ---------------------------

    /**
     * Get the name of the player
     *
     * @returns {string} name of the player
     */
    MyBullseyePlayer.prototype.getName = function () {
        return 'José Carlos Paiva';
    };

    /**
     * Initialize the player
     */
    MyBullseyePlayer.prototype.init = function () {


    };

    /**
     * Execute player action
     */
    MyBullseyePlayer.prototype.execute = function () {


    };

    // ------------------------ Skeleton --------------------------------

    exports.Player = MyBullseyePlayer;
}(this));

