
// ------------------------ Skeleton --------------------------------

load('asteroids/wrappers/es6/AsteroidsPlayer.js');

(function (exports) {'use strict';

    /**
     * Asteroids player in Javascript
     *
     * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
     */
    class Superman extends AsteroidsPlayer {

        // ---------------------- Your code below ---------------------------

        /**
         * Get the name of the player
         *
         * @returns {string} name of the player
         */
        getName() {
            return 'José Carlos Paiva';
        }

        /**
         * Initialize the player
         */
        init() {
            this.count = 0;
        }

        /**
         * Execute player action
         */
        execute() {

            if (this.count % 1000 === 0)
                this.thrust();

            this.firePrimary();

            this.steerLeft();

            this.shield();

            this.count++;
        }
    }


    // ------------------------ Skeleton --------------------------------

    exports.Player = Superman;
}(this));
