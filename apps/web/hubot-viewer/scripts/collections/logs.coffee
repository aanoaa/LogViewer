define [
  "jquery",
  "models/log"
], ($, Log) ->
  class Logs extends Backbone.Collection
    model: Log
    sync: (method, model, options) ->
      options.timeout = 10000
      options.dataType = 'jsonp'
      Backbone.sync(method, model, options)
