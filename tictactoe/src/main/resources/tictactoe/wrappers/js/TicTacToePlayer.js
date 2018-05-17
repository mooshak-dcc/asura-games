
var PlayerWrapper = require('../../../wrappers/js/PlayerWrapper');

/**
 * Functions that provide game-specific functionality to TicTacToe players
 *
 * @author José Carlos Paiva <code>josepaiva94@gmail.com</code>
 */
var TicTacToePlayer = function () {
    PlayerWrapper.call(this);
};

TicTacToePlayer.prototype = Object.create(PlayerWrapper.prototype);

/**
 * Update the state of the game on the player
 *
 * @param state_update {object} the state update
 */
TicTacToePlayer.prototype.update = function (state_update) {

    if (!state_update || !state_update.type)
        process.exit(-2);

    switch (state_update.type) {

        case 'PLAYER_X':
            if (this.getPlayerId() === state_update.object)
                this.piece = 'X';
            break;
        case 'PLAYER_O':
            if (this.getPlayerId() === state_update.object)
                this.piece = 'O';
            break;
        case 'BOARD':
            this.board = state_update.object;
            break;
    }
};

/**
 * Manage player lifecycle during the game, invoking the other methods when required
 */
TicTacToePlayer.prototype.run = function () {

    // read X player
    this.readAndUpdate();

    //read O player
    this.readAndUpdate();

    // read initial state
    this.readAndUpdate();

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
 * @param pos {number} Position of the board (between 1 and 9, counting from top-left
 *            to bottom-right)
 */
TicTacToePlayer.prototype.play = function (pos) {
    this.doAction("PLAY", [pos]);
};

/**
 * Check if position is free
 *
 * @param pos {number} Position of the board (between 1 and 9, counting from top-left
 *            to bottom-right)
 * @returns {boolean} position is free?
 */
TicTacToePlayer.prototype.isFree = function (pos) {
    return this.board[pos] === ' ';
};

/**
 * Check if position is occupied by opponent
 *
 * @param pos {number} Position of the board (between 1 and 9, counting from top-left
 *            to bottom-right)
 * @returns {boolean} position is occupied by opponent?
 */
TicTacToePlayer.prototype.isOpponent = function (pos) {
    return this.board[pos] !== this.piece && !this.isFree(pos);
};

/**
 * Check if position is occupied by the current player
 *
 * @param pos {number} Position of the board (between 1 and 9, counting from top-left
 *            to bottom-right)
 * @returns {boolean} position is occupied by self?
 */
TicTacToePlayer.prototype.isSelf = function (pos) {
    return board[pos] === piece;
};

module.exports = TicTacToePlayer;
