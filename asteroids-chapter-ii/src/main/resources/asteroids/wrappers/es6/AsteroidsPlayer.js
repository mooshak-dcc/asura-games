(function (exports) {'use strict';

    const DEFAULT_ACTION = {
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

        constructor () {
            super();

            this._state = {
                x: 0,
                y: 0,
                velocity: 0.0,
                heading: 0.0,
                energy: 0,
                health: 0,
                shieldCounter: 0
            };

            this._current_action = Object.assign({}, DEFAULT_ACTION);

            // radar of the ship - has different handlers for each type of detected actors
            this._radar = {
                asteroid_handler: undefined,
                ship_handler: undefined,
                bullet_handler: undefined
            };
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
                    this._processNearestActors(state_update.object.actors);
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

        /**
         * Actuators
         */

        shield () {
            this._current_action.shield = true;
        }


        /**
         * Radar handlers
         */

        onAsteroidDetected (handler) {
            this._radar.asteroid_handler = handler;
        }

        onShipDetected (handler) {
            this._radar.ship_handler = handler;
        }

        onBulletDetected (handler) {
            this._radar.bullet_handler = handler;
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

        _processNearestActors (actors) {

            if (!actors)
                return;

            for (let actor_type in actors) {
                if (actors.hasOwnProperty(actor_type)) {

                    switch (actor_type) {

                        case 'ASTEROID':
                            this._processNearestAsteroids(actors[actor_type]);
                            break;
                        case 'BULLET':
                            this._processNearestBullets(actors[actor_type]);
                            break;
                        case 'SHIP':
                            this._processNearestShips(actors[actor_type]);
                            break;
                    }
                }
            }
        }

        _processNearestAsteroids (asteroids) {

            if (!asteroids || !this._radar.asteroid_handler)
                return;

            asteroids.forEach(asteroid => {
                this._radar.asteroid_handler(asteroid);
            });
        }

        _processNearestShips (ships) {

            if (!ships || !this._radar.ship_handler)
                return;

            ships.forEach(ship => {
                this._radar.ship_handler(ship);
            });
        }

        _processNearestBullets (bullets) {

            if (!bullets || !this._radar.bullet_handler)
                return;

            bullets.forEach(bullet => {
                this._radar.bullet_handler(bullet);
            });
        }
    }

    exports.AsteroidsPlayer = AsteroidsPlayer;
}(this));
