
// ------------------------ Skeleton --------------------------------

var BullseyePlayer = require('./bullseye/wrappers/js/BullseyePlayer');

/**
 * Bullseye player in Javascript
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
function MyBullseyePlayer() {
    BullseyePlayer.call(this);
}

MyBullseyePlayer.prototype = Object.create(BullseyePlayer.prototype);

// ---------------------- Your code below ---------------------------

/**
 * Get the name of the player
 *
 * @returns {string} name of the player
 */
MyBullseyePlayer.prototype.getName = function () {
    return 'José C. Paiva';
};

/**
 * Initialize the player
 */
MyBullseyePlayer.prototype.init = function () {
};

/**
 * Execute player action
 */
MyBullseyePlayer.prototype.execute = function () {

    var lastHit = this.getLastHit();

    if (lastHit === undefined) {
        this.lastHorizAngle = Math.random() * 30 - 30;
        this.lastVertAngle = Math.random() * 30 - 30;
        this.shoot(this.lastHorizAngle, this.lastVertAngle);

        return;
    }

    if (lastHit[0] > 400) {
        this.lastHorizAngle = Math.max(this.lastHorizAngle - 10, -80);
    } else if (lastHit[0] < 200) {
        this.lastHorizAngle = Math.min(this.lastHorizAngle + 10, 80);
    } else
        this.lastHorizAngle = Math.max(Math.min(this.lastHorizAngle + Math.random() * 2.5, 80), -80);

    if (lastHit[1] > 400) {
        this.lastVertAngle = Math.max(this.lastVertAngle - 10, -80);
    } else if (lastHit[1] < 200) {
        this.lastVertAngle = Math.min(this.lastVertAngle + 10, 80);
    } else
        this.lastVertAngle = Math.max(Math.min(this.lastVertAngle + Math.random() * 2.5, 80), -80);

    this.shoot(this.lastHorizAngle, this.lastVertAngle);
};

// ------------------------ Skeleton --------------------------------

// export this player
module.exports = MyBullseyePlayer;
