define [
  "jquery",
  "models/log"
], ($, Log) ->
  class Logs extends Backbone.Collection
    model: Log
    sync: (method, model, options) ->
      options.dataType = 'jsonp'
      Backbone.sync(method, model, options)
