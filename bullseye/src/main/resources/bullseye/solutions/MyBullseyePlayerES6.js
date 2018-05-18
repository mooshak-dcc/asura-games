
// ------------------------ Skeleton --------------------------------

const BullseyePlayer = require('./bullseye/wrappers/es6/BullseyePlayer');

/**
 * Bullseye player in Javascript
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
class MyBullseyePlayer extends BullseyePlayer {

    // ---------------------- Your code below ---------------------------

    /**
     * Get the name of the player
     *
     * @returns {string} name of the player
     */
    getName() {
        return 'José C. Paiva';
    }

    /**
     * Initialize the player
     */
    init() {
    };

    /**
     * Execute player action
     */
    execute() {

        const lastHit = this.getLastHit();

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
    }

    // ------------------------ Skeleton --------------------------------

}

// export this player
module.exports = MyBullseyePlayer;
