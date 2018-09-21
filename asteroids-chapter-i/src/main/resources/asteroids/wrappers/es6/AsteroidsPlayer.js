(function (exports) {'use strict';

    const DEFAULT_ACTION = {
        thrust: false,
        steerLeft: false,
        steerRight: false,
        shield: false,
        fire: 0,
    };

    /**
     * Functions that provide game-specific functionality to Asteroids players
     *
     * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
     */
    class AsteroidsPlayer extends PlayerWrapper {

        constructor () {
            super();

            this._state = {
                position: {
                    x: 0,
                    y: 0
                },
                velocity: 0.0,
                heading: 0.0,
                energy: 0,
                health: 0,
                shieldCounter: 0
            };

            this._asteroids = [];

            this._current_action = Object.assign({}, DEFAULT_ACTION);
        }

        /**
         * Update the state of the game on the player
         *
         * @param state_update {object} the state update
         */
        update (state_update) {

            if (!state_update || state_update.type === undefined)
                return;

            switch (state_update.type) {
                case "FULL_UPDATE":
                    this._updateCurrentState(state_update.object);
                    this._updateAsteroids(state_update.object.asteroids);
                    break;
            }
        }

        /**
         * Manage player lifecycle during the game, invoking the other methods when required
         */
        run () {

            // game flow
            while (true) {

                this.readAndUpdate();
                this.execute();
                this.doAction("SHIP", [
                    this._current_action.thrust,
                    this._current_action.steerLeft,
                    this._current_action.steerRight,
                    this._current_action.shield,
                    this._current_action.fire,
                ]);
                this.sendAction();

                // reset current action
                this._current_action = Object.assign({}, DEFAULT_ACTION);
            }
        }

        /**
         * Sensors
         */

        state () {
            return Object.assign({}, this._state);
        }

        asteroids () {
            return [...this._asteroids];
        }

        /**
         * Actuators
         */

        thrust () {
            this._current_action.thrust = true;
        }

        steerLeft () {
            this._current_action.steerLeft = true;
        }

        steerRight () {
            this._current_action.steerRight = true;
        }

        shield () {
            this._current_action.shield = true;
        }

        firePrimary () {
            this._current_action.fire = 1;
        }

        /**
         * Helper Functions
         */

        _updateCurrentState (state) {
            this._state = {
                position: {
                    x: state.x,
                    y: state.y
                },
                velocity: state.velocity,
                heading: state.heading,
                energy: state.energy,
                health: state.health,
                shield_counter: state.shield_counter
            };
        }

        _updateAsteroids (asteroids) {
            this._asteroids = asteroids;
        }
    }

    exports.AsteroidsPlayer = AsteroidsPlayer;
}(this));
