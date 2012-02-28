(function() {
  var __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; }, __hasProp = Object.prototype.hasOwnProperty, __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor; child.__super__ = parent.prototype; return child; };

  define(["jquery", "collections/logs", "views/channel", "views/log"], function($, Logs, ChannelView, LogView) {
    var Router;
    return Router = (function() {

      __extends(Router, Backbone.Router);

      function Router() {
        this.specific = __bind(this.specific, this);
        this.today = __bind(this.today, this);
        this.channels = __bind(this.channels, this);
        Router.__super__.constructor.apply(this, arguments);
      }

      Router.prototype.initialize = function() {};

      Router.prototype.routes = {
        "": "channels",
        "!/channel/:channel": "today",
        "!/channel/:channel/:date": "specific"
      };

      Router.prototype.channels = function() {
        return $.ajax({
          url: 'http://irclog.yuni.in/vd/channel/',
          dataType: 'jsonp',
          success: function(data, textStatus, jqXHR) {
            return new ChannelView({
              data: data
            });
          }
        });
      };

      Router.prototype.today = function(channel) {
        var logs;
        logs = new Logs;
        logs.url = "http://irclog.yuni.in/vd/channel/" + channel;
        return logs.fetch({
          success: function(collection, res) {
            return new LogView({
              collection: collection,
              channel: channel
            });
          },
          error: function(collection, res) {
            return console.log("error: " + res);
          }
        });
      };

      Router.prototype.specific = function(channel, date) {
        return console.log('specific');
      };

      return Router;

    })();
  });

}).call(this);
