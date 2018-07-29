
(function (exports) {'use strict';

    const DEFAULT_ACTION = {
        thrust: false,
        steerLeft: false,
        steerRight: false,
        shield: false,
        fire: 0,
        fire_promise: undefined
    };

    const BULLET_ID_PREFIX = "bullet";

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
            this._fire_count = 0;

            // radar of the ship - has different handlers for each type of detected actors
            this._radar = {
                asteroid_handlers: [],
                ship_handlers: [],
                bullet_handlers: []
            };

            // promises for bullets indexed by bullet ID
            this._bullet_promises = new Map();
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
                    this._processBulletResults(state_update.object.bullets);
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

                // if fired, increase fire counter and add promise to map
                if (this._current_action.fire > 0) {
                    if (this._current_action.fire_promise) {
                        this._bullet_promises.set(BULLET_ID_PREFIX + this._fire_count,
                            this._current_action.fire_promise);
                    }

                    this._fire_count++;
                }

                // reset current action
                this._current_action = Object.assign({}, DEFAULT_ACTION);
            }
        }

        /**
         * Sensors
         */

        x () {
            return this._state.x;
        }

        y () {
            return this._state.y;
        }

        velocity () {
            return this._state.velocity;
        }

        heading () {
            return this._state.heading;
        }

        health () {
            return this._state.health;
        }

        energy () {
            return this._state.energy;
        }

        shieldCounter () {
            return this._state.shieldCounter;
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

            this._current_action.fire_promise = new DeferredPromise();

            /*this._current_action.fire_promise
                .then((result) => {
                    thrust();
                })
                .catch((e) => printErr('Oh my god!'));

            this._bullet_promises.set(BULLET_ID_PREFIX + this._fire_count, this._current_action.fire_promise);

            this._current_action.fire_promise.resolve("aa");*/

            let _resolve, _reject;
            let p = new Promise(((resolve, reject) => {
                _resolve = resolve;
                _reject = reject;
            }));
            this._current_action.fire_promise = { _resolve, _reject };

            p.then((result) => {
                this.log(result);
            })
                .catch(e => {
                    this.log('Oh my god!');
                });
            return p;
        }

        fireSecondary () {
            this._current_action.fire = 2;

            let promise = new DeferredPromise();
            this._current_action.fire_promise = promise;

            return promise;
        }


        /**
         * Helper Functions
         */

        _updateCurrentState (state) {
            this._state = {
                x: state.x,
                y: state.y,
                velocity: state.velocity,
                heading: state.heading,
                energy: state.energy,
                health: state.health,
            };
        }

        _processBulletResults (results) {

            if (!results)
                return;

            for (let bullet_id in results) {
                if (results.hasOwnProperty(bullet_id)) {
                    const result = results[bullet_id];
                    const p = this._bullet_promises.get(bullet_id);

                    if (p) {
                        if (result === "WEAPON_LOCKED") {
                            p._reject();
                        } else {
                            p._resolve(result);
                        }

                        this._bullet_promises.delete(bullet_id);
                    }
                }
            }
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
                        default:

                            break;
                    }
                }
            }
        }

        _processNearestAsteroids (asteroids) {

            if (!asteroids)
                return;

            asteroids.forEach(asteroid => {

                for (let i = 0; i < this._radar.asteroid_handlers; i++) {

                    const handler = this._radar.asteroid_handlers[i];
                    handler(asteroid);
                }
            });
        }

        _processNearestShips (ships) {

            if (!ships)
                return;

            ships.forEach(ship => {

                for (let i = 0; i < this._radar.ship_handlers; i++) {

                    const handler = this._radar.ship_handlers[i];
                    handler(ship);
                }
            });
        }

        _processNearestBullets (bullets) {

            if (!bullets)
                return;

            bullets.forEach(bullet => {

                for (let i = 0; i < this._radar.bullet_handlers; i++) {

                    const handler = this._radar.bullet_handlers[i];
                    handler(bullet);
                }
            });
        }
    }

    /**
     * A promise which can be resolved/rejected from the outside.
     */
    class DeferredPromise {
        constructor() {
            this._promise = new Promise((resolve, reject) => {
                this.resolve = resolve;
                this.reject = reject;
            });

            this.then = this._promise.then.bind(this._promise);
            this.catch = this._promise.catch.bind(this._promise);
            this[Symbol.toStringTag] = 'Promise';
        }
    }

    exports.AsteroidsPlayer = AsteroidsPlayer;
}(this));
