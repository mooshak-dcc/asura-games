(function (exports) {'use strict';

    /**
     * Functions that provide game-specific functionality to Lightning Pong players
     *
     * @author José C. Paiva <code>josepaiva94@gmail.com</code>
     */
    class LightningPongPlayer extends PlayerWrapper {

        constructor () {
            super();
            this._current_action = [0, 0, false];
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
                case "PONG":
                    this._state = Object.assign({}, state_update.object.board);
                    this._promise_succeeded = state_update.object.result === 'SUCCESS';
                    if (!this._promise_succeeded)
                        this._promise_failure_reason = state_update.object.result;
                    break;
            }

        }

        /**
         * Manage player lifecycle during the game, invoking the other methods when required
         */
        run () {

            this.readAndUpdate();
            this.start(this._state);
            this.doAction("PADDLE", this._current_action);
            this.sendAction();

            while (true) {

                this.readAndUpdate();

                if (!this._next_command)
                    return;

                if (this._promise_succeeded) {
                    this._next_command.resolve(this._state);
                } else {
                    this._next_command.reject(this._promise_failure_reason);
                }

                this.doAction("PADDLE", this._current_action);
                this.sendAction();

                // reset current action
                this._current_action = [0, 0, false];

                // reset promise results
                this._promise_succeeded = undefined;
                this._promise_failure_reason = undefined;
            }
        }

        /**
         * Player actions
         */

        idle () {
            this._current_action = [0, 0, false];
            return this._next_command = new DeferredPromise();
        }

        up () {
            this._current_action = [0, 1, false];
            return this._next_command = new DeferredPromise();
        }

        down () {
            this._current_action = [0, 2, false];
            return this._next_command = new DeferredPromise();
        }

        fire () {
            this._current_action = [0, 0, true];
            return this._next_command = new DeferredPromise();
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

    /**
     * Fake promise - synchronous calls
     */
    var PENDING = 'pending';
    var SEALED = 'sealed';
    var FULFILLED = 'fulfilled';
    var REJECTED = 'rejected';
    var NOOP = function(){};

    function isArray(value) {
        return Object.prototype.toString.call(value) === '[object Array]';
    }

    // sync calls
    var syncQueue = [];
    var syncTimer;

    function syncFlush(){
        // run promise callbacks
        for (var i = 0; i < syncQueue.length; i++)
            syncQueue[i][0](syncQueue[i][1]);

        // reset async asyncQueue
        syncQueue = [];
        syncTimer = false;
    }

    function syncCall(callback, arg){
        syncQueue.push([callback, arg]);

        if (!syncTimer)
        {
            syncTimer = true;
            syncFlush();
        }
    }

    function invokeResolver(resolver, promise) {
        function resolvePromise(value) {
            resolve(promise, value);
        }

        function rejectPromise(reason) {
            reject(promise, reason);
        }

        try {
            resolver(resolvePromise, rejectPromise);
        } catch(e) {
            rejectPromise(e);
        }
    }

    function invokeCallback(subscriber){
        var owner = subscriber.owner;
        var settled = owner.state_;
        var value = owner.data_;
        var callback = subscriber[settled];
        var promise = subscriber.then;

        if (typeof callback === 'function')
        {
            settled = FULFILLED;
            try {
                value = callback(value);
            } catch(e) {
                reject(promise, e);
            }
        }

        if (!handleThenable(promise, value))
        {
            if (settled === FULFILLED)
                resolve(promise, value);

            if (settled === REJECTED)
                reject(promise, value);
        }
    }

    function handleThenable(promise, value) {
        var resolved;

        try {
            if (promise === value)
                throw new TypeError('A promises callback cannot return that same promise.');

            if (value && (typeof value === 'function' || typeof value === 'object'))
            {
                var then = value.then;  // then should be retrived only once

                if (typeof then === 'function')
                {
                    then.call(value, function(val){
                        if (!resolved)
                        {
                            resolved = true;

                            if (value !== val)
                                resolve(promise, val);
                            else
                                fulfill(promise, val);
                        }
                    }, function(reason){
                        if (!resolved)
                        {
                            resolved = true;

                            reject(promise, reason);
                        }
                    });

                    return true;
                }
            }
        } catch (e) {
            if (!resolved)
                reject(promise, e);

            return true;
        }

        return false;
    }

    function resolve(promise, value){
        if (promise === value || !handleThenable(promise, value))
            fulfill(promise, value);
    }

    function fulfill(promise, value){
        if (promise.state_ === PENDING)
        {
            promise.state_ = SEALED;
            promise.data_ = value;

            syncCall(publishFulfillment, promise);
        }
    }

    function reject(promise, reason){
        if (promise.state_ === PENDING)
        {
            promise.state_ = SEALED;
            promise.data_ = reason;

            syncCall(publishRejection, promise);
        }
    }

    function publish(promise) {
        var callbacks = promise.then_;
        promise.then_ = undefined;

        for (var i = 0; i < callbacks.length; i++) {
            invokeCallback(callbacks[i]);
        }
    }

    function publishFulfillment(promise){
        promise.state_ = FULFILLED;
        publish(promise);
    }

    function publishRejection(promise){
        promise.state_ = REJECTED;
        publish(promise);
    }

    /**
     * @class
     */
    function Promise(resolver){
        if (typeof resolver !== 'function')
            throw new TypeError('Promise constructor takes a function argument');

        if (this instanceof Promise === false)
            throw new TypeError('Failed to construct \'Promise\': Please use the \'new\' operator, this object constructor cannot be called as a function.');

        this.then_ = [];

        invokeResolver(resolver, this);
    }

    Promise.prototype = {
        constructor: Promise,

        state_: PENDING,
        then_: null,
        data_: undefined,

        then: function(onFulfillment, onRejection){
            var subscriber = {
                owner: this,
                then: new this.constructor(NOOP),
                fulfilled: onFulfillment,
                rejected: onRejection
            };

            if (this.state_ === FULFILLED || this.state_ === REJECTED)
            {
                // already resolved, call callback async
                syncCall(invokeCallback, subscriber);
            }
            else
            {
                // subscribe
                this.then_.push(subscriber);
            }

            return subscriber.then;
        },

        catch: function(onRejection) {
            return this.then(null, onRejection);
        }
    };

    Promise.all = function(promises){
        var Class = this;

        if (!isArray(promises))
            throw new TypeError('You must pass an array to Promise.all().');

        return new Class(function(resolve, reject){
            var results = [];
            var remaining = 0;

            function resolver(index){
                remaining++;
                return function(value){
                    results[index] = value;
                    if (!--remaining)
                        resolve(results);
                };
            }

            for (var i = 0, promise; i < promises.length; i++)
            {
                promise = promises[i];

                if (promise && typeof promise.then === 'function')
                    promise.then(resolver(i), reject);
                else
                    results[i] = promise;
            }

            if (!remaining)
                resolve(results);
        });
    };

    Promise.race = function(promises){
        var Class = this;

        if (!isArray(promises))
            throw new TypeError('You must pass an array to Promise.race().');

        return new Class(function(resolve, reject) {
            for (var i = 0, promise; i < promises.length; i++)
            {
                promise = promises[i];

                if (promise && typeof promise.then === 'function')
                    promise.then(resolve, reject);
                else
                    resolve(promise);
            }
        });
    };

    Promise.resolve = function(value){
        var Class = this;

        if (value && typeof value === 'object' && value.constructor === Class)
            return value;

        return new Class(function(resolve){
            resolve(value);
        });
    };

    Promise.reject = function(reason){
        var Class = this;

        return new Class(function(resolve, reject){
            reject(reason);
        });
    };

    exports.LightningPongPlayer = LightningPongPlayer;
}(this));
