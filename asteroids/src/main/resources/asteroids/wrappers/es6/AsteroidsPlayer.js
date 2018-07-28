
(function (exports) {'use strict';

    const defaultAction = {
        thrust: false,
        steerLeft: false,
        steerRight: false,
        shield: false,
        fire: 0
    };

    /**
     * Functions that provide game-specific functionality to Asteroids players
     *
     * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
     */
    class AsteroidsPlayer extends PlayerWrapper {

        constructor() {
            super();

            this.state = {
                x: 0,
                y: 0,
                velocity: 0.0,
                heading: 0.0,
                energy: 0,
                health: 0,
                shieldCounter: 0
            };

            this.current_action = Object.assign({}, defaultAction);
            this.bulletsPromises = [];
        }

        /**
         * Update the state of the game on the player
         *
         * @param state_update {object} the state update
         */
        update(state_update) {

            if (!state_update || state_update.type === undefined)
                return;

            switch (state_update.type) {
                case "SHIP":
                    this.state = Object.assign(this.state, state_update.object);
                    break;
            }
        }

        /**
         * Manage player lifecycle during the game, invoking the other methods when required
         */
        run() {

            // game flow
            while (true) {

                this.readAndUpdate();
                this.execute();
                this.doAction("PLAY", [
                    this.current_action.thrust,
                    this.current_action.steerLeft,
                    this.current_action.steerRight,
                    this.current_action.shield,
                    this.current_action.fire,
                ]);
                this.sendAction();

                // reset current action
                this.current_action = Object.assign({}, defaultAction);
            }
        }

        thrust () {
            this.current_action.thrust = true;
        }

        steerLeft () {
            this.current_action.steerLeft = true;
        }

        steerRight () {
            this.current_action.steerRight = true;
        }

        shield () {
            this.current_action.shield = true;
        }

        firePrimary () {
            this.current_action.fire = 1;
        }

        fireSecondary () {
            this.current_action.fire = 2;
        }
    }

    exports.AsteroidsPlayer = AsteroidsPlayer;
}(this));
