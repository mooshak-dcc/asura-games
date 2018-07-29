
// ------------------------ Skeleton --------------------------------

load('asteroids/wrappers/es6/AsteroidsPlayer.js');

(function (exports) {'use strict';

    /**
     * Asteroids player in Javascript
     *
     * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
     */
    class Ironman extends AsteroidsPlayer {

        // ---------------------- Your code below ---------------------------

        /**
         * Get the name of the player
         *
         * @returns {string} name of the player
         */
        getName() {
            return 'Iron Man';
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

            // this.thrust();
            //this.log(this.health());

            let p = this.firePrimary();
            //printErr(JSON.stringify(p));
            p
                .then((result) => {
                    this.log(result);
                })
                .catch(e => {
                    this.log('Oh my god!');
                });
            //printErr(JSON.stringify(p));

            if (this.count % 100 === 0)
                this.steerRight();

            this.count++;
        }
    }


    // ------------------------ Skeleton --------------------------------

    exports.Player = Ironman;
}(this));
