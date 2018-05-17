
var PlayerWrapper = require('../../../wrappers/js/PlayerWrapper');

/**
 * Functions that provide game-specific functionality to Bullseye players
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
var BullseyePlayer = function () {
    PlayerWrapper.call(this);
};

BullseyePlayer.prototype = Object.create(PlayerWrapper.prototype);

/**
 * Update the state of the game on the player
 *
 * @param state_update {object} the state update
 */
BullseyePlayer.prototype.update = function (state_update) {

    if (state_update === undefined || state_update.type === undefined)
        return;

    switch (state_update.type) {

        case 'HIT':

            if (!state_update.object)
                process.exit(-2);

            var pos = state_update.object.split('\\s+');
            this.bullseyeX = pos[0];
            this.bullseyeY = pos[1];
            break;
    }
};

/**
 * Manage player lifecycle during the game, invoking the other methods when required
 */
BullseyePlayer.prototype.run = function () {

    // game flow
    while (true) {

        this.readAndUpdate();
        this.execute();
        this.sendAction();
    }
};

/**
 * Play in position pos of the board
 *
 * @param horizontalAngle Horizontal angle of the shot
 * @param verticalAngle Vertical angle of the shot
 */
BullseyePlayer.prototype.shoot = function (horizontalAngle, verticalAngle) {
    this.doAction("SHOOT", [horizontalAngle, verticalAngle]);
};

/**
 * Last position of the hit (x, y)
 *
 * @returns {Array} last position of the hit (x, y)
 */
BullseyePlayer.prototype.getLastHit = function () {

    if (this.bullseyeX === undefined || this.bullseyeY === undefined)
        return undefined;
    return [this.bullseyeX, this.bullseyeY];
};

module.exports = BullseyePlayer;
