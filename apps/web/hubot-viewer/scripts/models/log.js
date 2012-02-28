(function() {
  var __hasProp = Object.prototype.hasOwnProperty, __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor; child.__super__ = parent.prototype; return child; };

  define(["jquery", "order!http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.3.1/underscore-min.js", "order!http://cdnjs.cloudflare.com/ajax/libs/backbone.js/0.9.1/backbone-min.js"], function($) {
    var Log;
    return Log = (function() {

      __extends(Log, Backbone.Model);

      function Log() {
        Log.__super__.constructor.apply(this, arguments);
      }

      Log.prototype.initialize = function(attrs) {};

      return Log;

    })();
  });

}).call(this);
