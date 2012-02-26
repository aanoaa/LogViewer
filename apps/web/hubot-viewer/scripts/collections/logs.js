(function() {
  var __hasProp = Object.prototype.hasOwnProperty, __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor; child.__super__ = parent.prototype; return child; };

  define(["jquery", "models/log"], function($, Log) {
    var Logs;
    return Logs = (function() {

      __extends(Logs, Backbone.Collection);

      function Logs() {
        Logs.__super__.constructor.apply(this, arguments);
      }

      Logs.prototype.model = Log;

      Logs.prototype.sync = function(method, model, options) {
        options.timeout = 10000;
        options.dataType = 'jsonp';
        return Backbone.sync(method, model, options);
      };

      return Logs;

    })();
  });

}).call(this);
