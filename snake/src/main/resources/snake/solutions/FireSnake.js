// ------------------------ Skeleton --------------------------------

load('snake/wrappers/es6/SnakePlayer.js');

(function (exports) {'use strict';

    /**
     * Snake player in Javascript
     *
     * @author José C. Paiva <code>josepaiva94@gmail.com</code>
     */
    class FireSnake extends SnakePlayer {

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
            this.fruits = [];

            this.onNewLevel(() => {
                this.log("new level")
                this.fruits = [];
            });

            this.onNewFruit(fruit => this.fruits.push(fruit));
            this.onExpiredFruit(fruit => {
                this.fruits = this.fruits.filter(f => f.x !== fruit.x || f.y !== fruit.y)
            });


        }

        /**
         * Execute player action
         */
        execute() {

            if (this.fruits.length === 0) {

                this.preventBoundaries();
                return;
            }

            let target = this.fruits[0];

            let {snake, direction} = this.getSnake();

            if (snake[0].x > target.x && direction !== 'LEFT') {

                if (direction !== 'RIGHT' && !this.collidesSomething(snake[0].x -1, snake[0].y))
                    this.left();
                else if (snake[0].y > target.y && !this.collidesSomething(snake[0].x, snake[0].y -1))
                    this.up();
                else if (!this.collidesSomething(snake[0].x, snake[0].y +1))
                    this.down();
            } else if (snake[0].x < target.x && direction !== 'RIGHT') {

                if (direction !== 'LEFT' && !this.collidesSomething(snake[0].x +1, snake[0].y))
                    this.right();
                else if (snake[0].y > target.y && !this.collidesSomething(snake[0].x, snake[0].y -1))
                    this.up();
                else if (!this.collidesSomething(snake[0].x, snake[0].y +1))
                    this.down();
            } else {
                if (snake[0].y > target.y && !this.collidesSomething(snake[0].x, snake[0].y -1))
                    this.up();
                else if (!this.collidesSomething(snake[0].x, snake[0].y +1))
                    this.down();
                else {

                    if (direction === 'UP' && this.collidesSomething(snake[0].x, snake[0].y -1)) {

                        if (!this.collidesSomething(snake[0].x +1, snake[0].y))
                            this.right();
                        else
                            this.left();
                    } else if (direction === 'DOWN' && this.collidesSomething(snake[0].x, snake[0].y +1)) {

                        if (!this.collidesSomething(snake[0].x +1, snake[0].y))
                            this.right();
                        else
                            this.left();
                    } else if (direction === 'LEFT' && this.collidesSomething(snake[0].x -1, snake[0].y)) {

                        if (!this.collidesSomething(snake[0].x, snake[0].y +1))
                            this.up();
                        else
                            this.down();
                    } else if (direction === 'RIGHT' && this.collidesSomething(snake[0].x +1, snake[0].y)) {

                        if (!this.collidesSomething(snake[0].x, snake[0].y +1))
                            this.up();
                        else
                            this.down();
                    }
                }
            }

            this.log(snake[0])
        }

        preventBoundaries () {
            let {width, height} = this.getPuzzle();
            let {snake, direction} = this.getSnake();

            if ((snake[0].x >= width - 1 || this.collidesSomething(snake[0].x +1, snake[0].y)) && direction === 'RIGHT') {

                if (snake[0].y >= height/2)
                    this.up();
                else
                    this.down();
            } else if ((snake[0].x <= 0 || this.collidesSomething(snake[0].x -1, snake[0].y)) && direction === 'LEFT') {

                if (snake[0].y >= height/2)
                    this.up();
                else
                    this.down();
            } else if ((snake[0].y <= 0 || this.collidesSomething(snake[0].x, snake[0].y -1)) && direction === 'UP') {

                if (snake[0].x >= width/2)
                    this.left();
                else
                    this.right();
            } else if ((snake[0].y >= height - 1 || this.collidesSomething(snake[0].x, snake[0].y +1)) && direction === 'DOWN') {

                if (snake[0].x >= width/2)
                    this.left();
                else
                    this.right();
            }
        }

        collidesSomething(x, y) {

            let {width, height, blocks} = this.getPuzzle();

            for (let i = 0; i < blocks.length; i++) {

                if (blocks[i].x === x && blocks[i].y === y)
                    return true;
            }

            let {snake, direction} = this.getSnake();

            for (let i = 0; i < snake.length; i++) {

                if (snake[i].x === x && snake[i].y === y)
                    return true;
            }

            if (x >= width)
                return true;
            else if (x < 0)
                return true;
            else if (y < 0)
                return true;
            else if (y >= height)
                return true;

            return false;
        }
    }


    // ------------------------ Skeleton --------------------------------

    exports.Player = FireSnake;
}(this));
