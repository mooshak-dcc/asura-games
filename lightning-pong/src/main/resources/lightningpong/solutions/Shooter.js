// ------------------------ Skeleton --------------------------------

load('lightningpong/wrappers/es6/LightningPongPlayer.js');

(function (exports) {
    'use strict';

    /**
     * Lightning Pong player in Javascript
     */
    class Shooter extends LightningPongPlayer {

        // ---------------------- Your code below ---------------------------

        /**
         * Get the name of the player
         *
         * @returns {string} name of the player
         */
        getName() {
            return 'Shooter';
        }

        /**
         * Initialize the player
         */
        init() {
            this.direction = 'up';
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
                .then((state) => this.shooting(state))
                .catch((reason) => this.rejected(reason));
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

        shooting (state) {

            this.fire()
                .then((state) => this.move(state))
                .catch((reason => this.rejected(reason)));
        }

        move ({ball, me, opponent}) {
            if (this.direction === 'up')
                this.goUp({ball, me, opponent});
            else
                this.goDown({ball, me, opponent});

        }

        goUp (state) {
            this.up()
                .then((state) => this.goUp(state))
                .catch((reason) => this.rejected(reason));
        }

        goDown (state) {
            this.down()
                .then((state) => this.goDown(state))
                .catch((reason) => this.rejected(reason));
        }

        /**
         * Sample method that can be provided to the promise to be executed
         * when promise rejects. Feel free to rename it, create additional
         * methods for rejecting promises, etc.
         *
         * @param reason {string} reason for failure. One of FAILURE_FROZEN|FAILURE_RECHARGING|FAILURE_OUT_OF_BOUNDS
         */
        rejected(reason) {

            if (reason === 'FAILURE_RECHARGING'){

                if (this.direction === 'up') {

                    this.down()
                        .then((state) => this.goDown(state))
                        .catch((reason) => this.rejected(reason));

                    this.direction = 'down';
                } else {

                    this.up()
                        .then((state) => this.goUp(state))
                        .catch((reason) => this.rejected(reason));

                    this.direction = 'up';
                }
            }
            else if (reason === 'FAILURE_OUT_OF_BOUNDS')
                this.fire()
                    .then((state) => this.move(state))
                    .catch((reason => this.rejected(reason)));
            else
                this.idle()
                    .then((state) => this.move(state))
                    .catch((reason => this.rejected(reason)));
        }
    }


    // ------------------------ Skeleton --------------------------------

    exports.Player = Shooter;
}(this));
