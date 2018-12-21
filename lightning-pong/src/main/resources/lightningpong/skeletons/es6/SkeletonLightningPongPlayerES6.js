// ------------------------ Skeleton --------------------------------

load('lightningpong/wrappers/es6/LightningPongPlayer.js');

(function (exports) {
    'use strict';

    /**
     * Lightning Pong player in Javascript
     */
    class MyLightningPongPlayer extends LightningPongPlayer {

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

        }

        /**
         * Starting state of the paddle
         *
         * @param initial_state {object} the state of the board e.g.,
         *      {
         *          ball: { x: number, y: number },
         *          me: { x: number, y: number, larger: 0, faster: 0, frozen: 0 },
         *          opponent: { x: number, y: number, larger: false, faster: false, frozen: false },
         *          powerUps: [ ],
         *          lasers: [ ],
         *          score: { me: 0, opponent: 0 }
         *      }
         */
        start(initial_state) {

            this.idle()
                .then((state) => this.resolved(state))
                .catch((err) => this.rejected(err));
        }

        /**
         * Sample method that can be provided to the promise to be executed
         * when promise resolves. Feel free to rename it, create additional
         * methods for resolving promises, use object destructuring to get
         * an easier to use representation of the state, etc.
         *
         * @param state {object} the state of the board e.g.,
         *      {
         *          ball: { x: number, y: number },
         *          me: { x: number, y: number, larger: number, faster: number, frozen: number },
         *          opponent: { x: number, y: number, larger: boolean, faster: boolean, frozen: boolean },
         *          powerUps: [ { x: number, y: number, type: string (one of LARGER_PADDLE|FASTER_PADDLE|FASTER_BALL) } ],
         *          lasers: [ { x: number, y: number } ],
         *          score: { me: number, opponent: number }
         *      }
         */
        resolved(state) {

        }

        /**
         * Sample method that can be provided to the promise to be executed
         * when promise rejects. Feel free to rename it, create additional
         * methods for rejecting promises, etc.
         *
         * @param reason {string} reason for failure. One of FAILURE_FROZEN|FAILURE_RECHARGING|FAILURE_OUT_OF_BOUNDS
         */
        rejected(reason) {

        }
    }


    // ------------------------ Skeleton --------------------------------

    exports.Player = MyLightningPongPlayer;
}(this));
