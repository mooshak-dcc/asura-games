(function (exports) {'use strict';

    /**
     * Functions that provide game-specific functionality to Snake players
     *
     * @author José C. Paiva <code>josepaiva94@gmail.com</code>
     */
    class SnakePlayer extends PlayerWrapper {

        constructor() {
            super();

            this._board = undefined;
            this._snake = undefined;

            this._new_fruit_handler = undefined;
            this._new_level_handler = undefined;
            this._expired_fruit_handler = undefined;

            this._action = "IDLE";
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
                case "BOARD":

                    this._board = {
                        width: state_update.object.width,
                        height: state_update.object.height,
                        blocks: state_update.object.blocks
                    };

                    if (state_update.object.new_level)
                        this._newLevel();

                    break;
                case "SNAKE":

                    this._snake = {
                        snake: state_update.object.snake,
                        direction: state_update.object.direction
                    };

                    if (state_update.object.new_fruits)
                        state_update.object.new_fruits.forEach(fruit => this._newFruit(fruit));

                    if (state_update.object.expired_fruits)
                        state_update.object.expired_fruits.forEach(fruit => this._expiredFruit(fruit));

                    break;
            }
        }

        /**
         * Manage player lifecycle during the game, invoking the other methods when required
         */
        run() {

            while (true) {

                // board
                this.readAndUpdate();

                // snake
                this.readAndUpdate();

                this.execute();
                this.doAction(this._action);
                this.sendAction();

                // reset current action
                this._action = "IDLE";
            }
        }

        /**
         * Information
         */
        getPuzzle() {
            return this._board;
        }

        getSnake() {
            return this._snake;
        }

        /**
         * Install handlers
         */
        onNewLevel(handler) {
            this._new_level_handler = handler;
        }

        onNewFruit(handler) {
            this._new_fruit_handler = handler;
        }

        onExpiredFruit(handler) {
            this._expired_fruit_handler = handler;
        }

        /**
         * Actions
         */

        up () {
            this._action = "UP";
        }

        down () {
            this._action = "DOWN";
        }

        left () {
            this._action = "LEFT";
        }

        right () {
            this._action = "RIGHT";
        }

        _newLevel() {
            if (this._new_level_handler)
                this._new_level_handler();
        }

        _newFruit(fruit) {
            if (this._new_fruit_handler)
                this._new_fruit_handler(fruit);
        }

        _expiredFruit(fruit) {
            if (this._expired_fruit_handler)
                this._expired_fruit_handler(fruit);
        }
    }

    exports.SnakePlayer = SnakePlayer;
}(this));
