define [
  "jquery",
  "order!http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.3.1/underscore-min.js",
  "order!http://cdnjs.cloudflare.com/ajax/libs/backbone.js/0.9.1/backbone-min.js"
], ($) ->
  class Log extends Backbone.Model
    idAttribute: '_id'
    initialize: (attrs) ->
