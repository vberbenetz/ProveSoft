'use strict';

function customTimer($timeout) {

    function CustomTimer(callback, duration, invokeApply) {

        this._callback = callback;
        this._duration = (duration || 0);
        this._invokeApply = (invokeApply!==false);

        this._timer = null;
    }

    CustomTimer.prototype = {

        constructor: CustomTimer,

        isActive: function() {
            return(!! this._timer);
        },

        restart: function() {
            this.stop();
            this.start();
        },

        start: function() {
            var self = this;

            this._timer = $timeout(
                function handleTimeoutResolve() {
                    try {
                        self._callback.call(null);
                    }
                    finally {
                        self = self._timer = null;
                    }
                },
                this._duration,
                this._invokeApply
            );
        },

        stop: function() {
            $timeout.cancel(this._timer);
            this._timer = false;
        },

        teardown: function() {
            this.stop();
            this._callback = null;
            this._duration = null;
            this._invokeApply = null;
            this._timer = null;
        }
    };

    function customTimerFactory(callback, duration, invokeApply) {
        return( new CustomTimer(callback, duration, invokeApply) );
    }

    customTimerFactory.CustomTimer = CustomTimer;
}

angular
    .module('provesoft')
    .factory('customTimer', customTimer);