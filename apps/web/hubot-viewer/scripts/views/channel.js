(function() {
  var __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; }, __hasProp = Object.prototype.hasOwnProperty, __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor; child.__super__ = parent.prototype; return child; };

  define(["jquery", "order!http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.3.1/underscore-min.js", "order!http://cdnjs.cloudflare.com/ajax/libs/backbone.js/0.9.1/backbone-min.js"], function($) {
    var ChannelView;
    return ChannelView = (function() {

      __extends(ChannelView, Backbone.View);

      function ChannelView() {
        this.render = __bind(this.render, this);
        ChannelView.__super__.constructor.apply(this, arguments);
      }

      ChannelView.prototype.id = 'channels';

      ChannelView.prototype.tagName = 'ul';

      ChannelView.prototype.initialize = function() {
        return this.render();
      };

      ChannelView.prototype.template = _.template($('#channel-item-template').html());

      ChannelView.prototype.render = function() {
        var channel, html, _i, _len, _ref;
        html = '';
        _ref = this.options.data;
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          channel = _ref[_i];
          html += this.template({
            channel: channel.split('#')[1]
          });
        }
        $(this.el).html(html);
        $('header').html(this.el);
        this.delegateEvents();
        return this;
      };

      return ChannelView;

    })();
  });

}).call(this);
