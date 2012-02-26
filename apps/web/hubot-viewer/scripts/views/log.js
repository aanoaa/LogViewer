(function() {
  var __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; }, __hasProp = Object.prototype.hasOwnProperty, __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor; child.__super__ = parent.prototype; return child; };

  define(["jquery", "order!http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.3.1/underscore-min.js", "order!http://cdnjs.cloudflare.com/ajax/libs/backbone.js/0.9.1/backbone-min.js"], function($) {
    var LogView;
    return LogView = (function() {

      __extends(LogView, Backbone.View);

      function LogView() {
        this.render = __bind(this.render, this);
        LogView.__super__.constructor.apply(this, arguments);
      }

      LogView.prototype.initialize = function() {
        return this.render();
      };

      LogView.prototype.template = _.template($('#messages-item-template').html());

      LogView.prototype.render = function() {
        var html, messages;
        var _this = this;
        html = _.template($('#messages-template').html())({});
        messages = '';
        _.each(this.collection.models, function(log) {
          var dt;
          dt = new Date(log.get('timestamp'));
          log.set({
            time: "" + (dt.getHours()) + ":" + (dt.getMinutes())
          });
          return messages += _this.template(log.attributes);
        });
        $(this.el).html(html).find('tbody').append(messages);
        $('#content').html(this.el);
        this.delegateEvents();
        return this;
      };

      return LogView;

    })();
  });

}).call(this);
