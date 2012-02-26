define [
  "jquery",
  "collections/logs",
  "views/channel",
  "views/log"
], ($, Logs, ChannelView, LogView) ->
  class Router extends Backbone.Router
    initialize: ->
    routes:
      "": "channels"
      "!/channel/:channel": "today"
      "!/channel/:channel/:date": "specific"
    channels: =>
      $.ajax
        url: 'http://localhost:8888/channel'
        dataType: 'jsonp'
        success: (data, textStatus, jqXHR) ->
          new ChannelView { data }
    today: (channel) =>
      logs = new Logs
      logs.url = "http://localhost:8888/channel/#{channel}"
      logs.fetch
        success: (collection, res) ->
          new LogView { collection: collection, channel: channel }
        error: (collection, res) ->
          console.log "error: #{res}" # TODO: better error handling
    specific: (channel, date) =>
      console.log 'specific'
